package dev.atsushieno.cipackageinstaller

import android.content.pm.PackageInstaller
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import org.kohsuke.github.GHArtifact
import org.kohsuke.github.GHEvent
import org.kohsuke.github.GHWorkflowRun
import java.io.File
import java.util.zip.ZipFile

class GitHubRepositoryInformation(
    val owner: GitHubRepositoryStore,
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

    private val variantsList = listOf(GitHubArtifactApplicationArtifact(this))
    override val variants: List<ApplicationArtifact>
        get() = variantsList
}

class GitHubArtifactApplicationArtifact internal constructor(repository: GitHubRepository)
    : GitHubApplicationArtifact(repository) {
    private val workflowRun: GHWorkflowRun
    private val artifact: GHArtifact

    override val artifactName: String
        get() = "${artifact.name} (workflow #${workflowRun.runNumber})"

    override val versionId: String
        get() = workflowRun.headCommit.id

    override val artifactSizeInBytes: Long
        get() = artifact.sizeInBytes

    override fun downloadAsFile(dst: File) {
        artifact.download { inStream ->
            AppModel.copyStream(inStream, dst)
        }
    }

    init {
        val info = repository.info
        val repoName = repository.repoName
        val repoData = info.owner.github.getRepository(repoName)

        val run =
            repoData.queryWorkflowRuns()
                .branch("main")
                .event(GHEvent.PUSH)
                .status(GHWorkflowRun.Status.COMPLETED)
                .list()
                .filter { r -> r.conclusion == GHWorkflowRun.Conclusion.SUCCESS }
                .firstOrNull { r -> r.listArtifacts().toArray().any { a -> !a.isExpired } }
                ?: throw CIPackageInstallerException("GitHub repository $repoName does not have any workflow runs yet")
        workflowRun = run

        println("---- Workflow Run ----")
        println(run.headCommit.id)
        println(run.url)

        artifact = run.listArtifacts().toArray().first()

        println("---- artifact ----")
        println(artifact.name)
        println(artifact.createdAt)
        println(artifact.sizeInBytes)
        println(artifact.archiveDownloadUrl)
    }
}

abstract class GitHubApplicationArtifact internal constructor(override val repository: GitHubRepository)
    : ApplicationArtifact(repository) {

    override fun toPackageInstallerSessionParams() : PackageInstaller.SessionParams {
        val info = repository.info
        val p = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
        p.setAppPackageName(info.packageName)
        p.setAppLabel(info.appLabel)
        p.setOriginatingUri(Uri.parse("https://github.com/${info.account}/${info.repository}/"))
        p.setReferrerUri(Uri.parse(info.owner.referrer))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            p.setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_REQUIRED)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            p.setPackageSource(PackageInstaller.PACKAGE_SOURCE_OTHER)
        }
        return p
    }

    override val typeName: String
        get() = "Latest GitHub Actions Artifact"

    abstract val artifactSizeInBytes: Long

    abstract fun downloadAsFile(dst: File)

    override fun downloadApp(): File {
        val tmpZipFile = File.createTempFile("GHTempArtifact", ".zip")
        try {
            downloadAsFile(tmpZipFile)
            val zipFile = ZipFile(tmpZipFile)
            val entry = zipFile.entries().iterator().asSequence().firstOrNull {
                it.name.endsWith(".apk") || it.name.endsWith(".aab")
            }
            if (entry != null) {
                val tmpAppFile = File.createTempFile("GHTempApp", "." + File(entry.name).extension) // apk or aab

                println("Downloading ${entry.name} ...")
                val inAppStream = zipFile.getInputStream(entry)
                AppModel.copyStream(inAppStream, tmpAppFile)
                if (!tmpAppFile.exists() || tmpAppFile.length() != entry.size)
                    throw CIPackageInstallerException("Artifact uncompressed size mismatch: expected ${artifactSizeInBytes}, got ${tmpAppFile.length()}")

                return tmpAppFile
            } else
                throw CIPackageInstallerException("... app entry in the artifact not found at run $artifactName for ${repository.repoName}")
        } finally {
            tmpZipFile.delete()
        }
    }
}

@Suppress("unused")
class CIPackageInstallerException : Exception {
    constructor() : this("CIPackageInstallerException occurred")
    constructor(message: String) : this (message, null)
    constructor(message: String, innerException: Exception?) : super(message, innerException)
}
