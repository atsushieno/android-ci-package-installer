package dev.atsushieno.cipackageinstaller

import android.content.pm.PackageInstaller
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Log
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import org.kohsuke.github.GHArtifact
import org.kohsuke.github.GHAsset
import org.kohsuke.github.GHEvent
import org.kohsuke.github.GHRelease
import org.kohsuke.github.GHWorkflowRun
import java.io.File
import java.util.zip.ZipFile

class GitHubRepositoryInformation(
    val account: String,
    val repository: String,
    override val packageName: String,
    override val appLabel: String
) : RepositoryInformation() {
    override val name: String = "$account/$repository"

    override val icon: Bitmap?
        get() = null // TODO: implement

    override fun createRepository(): Repository = GitHubRepository(this)
}

class GitHubRepository internal constructor(override val info: GitHubRepositoryInformation)
    : Repository(info) {

    val repoName = info.account + "/" + info.repository

    private val variantsList: List<ApplicationArtifact>
    override val variants: List<ApplicationArtifact>
        get() = variantsList

    override fun getPackageInstallerSessionParams(download: ApplicationArtifact) : PackageInstaller.SessionParams {
        val p = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
        p.setAppPackageName(info.packageName)
        p.setAppLabel(info.appLabel)
        p.setOriginatingUri(Uri.parse("https://github.com/${info.account}/${info.repository}/"))
        p.setReferrerUri(Uri.parse(AppModel.installerSessionReferrer))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            p.setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            p.setPackageSource(PackageInstaller.PACKAGE_SOURCE_OTHER)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            p.setRequestUpdateOwnership(true)
        }
        return p
    }

    init {
        val releases = GitHubReleaseApplicationArtifact.createReleases(this, 3)
        // it may or may not exist (regardless of the authentication status)
        val artifact = if (AppModel.githubApplicationStore.github.isAnonymous) null
            else GitHubArtifactApplicationArtifact.tryCreate(this)

        variantsList = if (artifact != null) releases + artifact else releases
    }
}

@Serializable
data class GitHubLatestWorkflowRunArtifactInfo(val account: String, val repository: String, val packageName: String, val appLabel: String, val workflowRunId: Long, val artifactId: Long)

class GitHubArtifactApplicationArtifact internal constructor(
    private val workflowRun: GHWorkflowRun,
    private val artifact: GHArtifact,
    repository: GitHubRepository
) : ApplicationArtifact(repository) {
    companion object {
        fun tryCreate(repository: GitHubRepository): GitHubArtifactApplicationArtifact? = try {
            val repoName = repository.repoName
            val repoData = AppModel.githubApplicationStore.github.getRepository(repoName)

            val workflowRun =
                repoData.queryWorkflowRuns()
                    .branch("main")
                    .event(GHEvent.PUSH)
                    .status(GHWorkflowRun.Status.COMPLETED)
                    .list()
                    .filter { r -> r.conclusion == GHWorkflowRun.Conclusion.SUCCESS }
                    .firstOrNull { r -> r.listArtifacts().toArray().any { a -> !a.isExpired } }
                    ?: throw CIPackageInstallerException("GitHub repository $repoName does not have any workflow runs yet")
            val artifact = workflowRun.listArtifacts().toArray().first()
            GitHubArtifactApplicationArtifact(workflowRun, artifact, repository)
        } catch (ex: CIPackageInstallerException) {
            AppModel.logger.logError(ex.message ?: "CIPackageInstallerException: $ex", ex)
            null
        }
    }

    override val typeName: String
        get() = "Latest GitHub Actions Artifact"

    override val artifactName: String
        get() = "${artifact.name} (workflow #${workflowRun.runNumber})"

    override val versionId: String
        get() = workflowRun.headCommit.id.substring(0, 7)

    override val artifactSizeInBytes: Long
        get() = artifact.sizeInBytes

    override fun downloadApp(): File {
        val tmpZipFile = File.createTempFile("GHTempArtifact", ".zip")
        try {
            artifact.download { inStream ->
                AppModel.copyStream("on downloading $artifactName", inStream, tmpZipFile)
            }
            val zipFile = ZipFile(tmpZipFile)
            val entry = zipFile.entries().iterator().asSequence().firstOrNull {
                it.name.endsWith(".apk") || it.name.endsWith(".aab")
            }
            if (entry != null) {
                val tmpAppFile =
                    File.createTempFile("GHTempApp", "." + File(entry.name).extension) // apk or aab

                Log.d(AppModel.LOG_TAG, "Downloading ${entry.name} ...")
                val inAppStream = zipFile.getInputStream(entry)
                AppModel.copyStream("on downloading $artifactName", inAppStream, tmpAppFile)
                if (!tmpAppFile.exists() || tmpAppFile.length() != entry.size)
                    throw CIPackageInstallerException("Artifact uncompressed size mismatch: expected ${artifactSizeInBytes}, got ${tmpAppFile.length()}")
                return tmpAppFile
            } else
                throw CIPackageInstallerException("... app entry in the artifact not found at run $artifactName for ${repository.info.name}")
        } catch (ex: OutOfMemoryError) {
            AppModel.logger.logError("Artifact download failed: OutOfMemoryError. It likely means the GitHub API for Java stored more bytes than OOM killer limitation: $ex")
            throw ex
        } catch (ex: Exception) {
            AppModel.logger.logError("Artifact download failed: $ex", ex)
            throw ex
        } finally {
            tmpZipFile.delete()
        }
    }

    override val articactInfoType: String = GitHubLatestWorkflowRunArtifactInfo::class.java.name

    override fun serializeToString(): String {
        val info = repository.info as GitHubRepositoryInformation
        val artifactInfo = GitHubLatestWorkflowRunArtifactInfo(info.account, info.repository, info.packageName, info.appLabel, workflowRun.id, artifact.id)
        return Json.encodeToString(artifactInfo)
    }
}

@Serializable
class GitHubReleaseApplicationArtifactInfo(val account: String, val repository: String, val packageName: String, val appLabel: String, val releaseTag: String)

class GitHubReleaseApplicationArtifact
internal constructor(repository: GitHubRepository,
                     private val release: GHRelease,
                     private val asset: GHAsset)
    : ApplicationArtifact(repository) {

    companion object {
        private fun getAsset(release: GHRelease) =
            release.listAssets()?.firstOrNull { it.name.endsWith(".apk") || it.name.endsWith(".aab") }

        fun createReleases(repository: GitHubRepository, count: Int) : List<GitHubReleaseApplicationArtifact> {
            val repoData = AppModel.githubApplicationStore.github.getRepository(repository.repoName)

            return repoData.listReleases().map { release ->
                Pair(release, getAsset(release))
            }
                .filter { it.second != null }.take(count)
                .map {
                    GitHubReleaseApplicationArtifact(repository, it.first, it.second!!)
                }
        }

        fun tryCreate(repository: GitHubRepository, releaseTag: String): ApplicationArtifact {
            val repoData = AppModel.githubApplicationStore.github.getRepository(repository.repoName)
            val release = repoData.getReleaseByTagName(releaseTag)
            return GitHubReleaseApplicationArtifact(repository, release, getAsset(release) ?: throw CIPackageInstallerException("Specified release ${release.name} for ${repository.repoName} does not contain an expected application package."))
        }
    }

    override val typeName: String
        get() = "GitHub Release: " + release.name

    override val artifactSizeInBytes: Long
        get() = asset.size

    override fun downloadApp(): File {
        val tmpAppFile = File.createTempFile("GHTempArtifact", "." + File(Uri.parse(asset.browserDownloadUrl).path!!).extension) // .apk or .aab
        val client = OkHttpClient()
        with(client.newCall(Request.Builder().url(asset.browserDownloadUrl.toString()).build()).execute()) {
            val stream = this.body?.byteStream() ?: throw CIPackageInstallerException("Failed to download asset for the latest release ${release.name} from ${asset.url}")
            AppModel.copyStream("on downloading $artifactName", stream, tmpAppFile)
        }
        if (!tmpAppFile.exists() || tmpAppFile.length() != artifactSizeInBytes)
            throw CIPackageInstallerException("Release artifact size mismatch: expected ${artifactSizeInBytes}, got ${tmpAppFile.length()}")
        return tmpAppFile
    }

    override val artifactName: String
        get() = asset.name

    override val versionId: String
        get() = release.tagName

    override val articactInfoType: String = GitHubReleaseApplicationArtifactInfo::class.java.name

    override fun serializeToString(): String {
        val info = repository.info as GitHubRepositoryInformation
        val artifactInfo = GitHubReleaseApplicationArtifactInfo(info.account, info.repository, info.packageName, info.appLabel, release.tagName)
        return Json.encodeToString(artifactInfo)
    }
}
