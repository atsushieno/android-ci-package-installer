package dev.atsushieno.cipackageinstaller

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.FileUtils
import androidx.annotation.RequiresApi
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.Executor

val AppModel by lazy { AppModelFactory.create() }

abstract class ApplicationModel {
    companion object {
        private const val PENDING_INTENT_REQUEST_CODE = 1
        private const val PENDING_PREAPPROVAL_REQUEST_CODE = 2
    }
    abstract val LOG_TAG: String
    abstract val installerSessionReferrer: String

    // it is made overridable
    open val applicationStore: RepositoryCatalogProvider
        get() = githubApplicationStore

    val preApprovalEnabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE || Build.VERSION.CODENAME == "UpsideDownCake"

    private fun createSharedPreferences(context: Context) : SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(context, "AndroidCIPackageInstaller", masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
    }

    // FIXME: it is too tied to GitHub. We should provide somewhat more generic way to provide credentials to other stores.
    fun getGitHubCredentials(context: Context) : GitHubRepositoryCatalogProvider.GitHubCredentials {
        val sp = createSharedPreferences(context)
        val user = sp.getString("GITHUB_USER", "") ?: ""
        val pat = sp.getString("GITHUB_PAT", "") ?: ""
        return GitHubRepositoryCatalogProvider.GitHubCredentials(user, pat)
    }

    // FIXME: it is too tied to GitHub. We should provide somewhat more generic way to provide credentials to other stores.
    fun setGitHubCredentials(context: Context, username: String, pat: String) {
        val sp = createSharedPreferences(context)
        val edit = sp.edit()
        edit.putString("GITHUB_USER", username)
        edit.putString("GITHUB_PAT", pat)
        edit.apply()

        githubApplicationStore.updateCredentials(username, pat)
    }

    fun copyStream(inFS: InputStream, outFile: File) =  copyStream("", inFS, outFile, 0)

    fun copyStream(label: String, inFS: InputStream, outFile: File, copySize: Long) {
        FileOutputStream(outFile).use { copyStream(label, inFS, it, copySize) }
    }

    fun copyStream(inFS: InputStream, outFS: OutputStream) = copyStream("", inFS, outFS, 0)

    fun copyStream(label: String, inFS: InputStream, outFS: OutputStream, copySize: Long) {
        val bytes = ByteArray(4096)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (!label.isEmpty())
                FileUtils.copy(inFS, outFS, null, MyExecutor, ProgressListener(label, copySize))
            else
                FileUtils.copy(inFS, outFS)
        } else {
            while (inFS.available() > 0) {
                val size = inFS.read(bytes)
                outFS.write(bytes, 0, size)
            }
        }
    }

    object MyExecutor : Executor {
        // no thread constraints
        override fun execute(command: Runnable?) { command?.run() }
    }
    @RequiresApi(Build.VERSION_CODES.Q)
    class ProgressListener(private val label: String, private val size: Long) : FileUtils.ProgressListener {
        var nextProgressReport = (size / 10).coerceAtLeast(1048576)
        override fun onProgress(progress: Long) {
            if (progress > nextProgressReport) {
                AppModel.logger.logInfo("$label [${(100 * progress / size).toInt()}%: $progress / $size]")
                nextProgressReport += size / 10
            }
        }
    }

    // Process permissions and then download and launch pending installation intent
    // (that may involve user interaction).
    fun performDownloadAndInstallation(context: Context, download: ApplicationArtifact) {
        val request = OneTimeWorkRequestBuilder<DownloadAndInstallWorker>()
            .setInputData(workDataOf(
                DownloadAndInstallWorker.INPUT_DATA_ARTIFACT_TYPE to download.articactInfoType,
                DownloadAndInstallWorker.INPUT_DATA_DOWNLOAD to download.serializeToString()))
            .build()
        WorkManager.getInstance(context).enqueue(request)
    }

    fun performUninstallPackage(context: Context, repo: Repository) {
        val installer = context.packageManager.packageInstaller
        val intent = Intent(context, PackageInstallerReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, PENDING_INTENT_REQUEST_CODE,
            intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
        installer.uninstall(repo.info.packageName, pendingIntent.intentSender)
    }

    // provide access to GitHub specific properties such as `guthubRepositories`
    val githubApplicationStore  by lazy { GitHubRepositoryCatalogProvider() }

    // This method is used to find the relevant packages that are already installed in an explicit way.
    // (We cannot simply query existing (installed) apps that exposes users privacy.)
    // Override it to determine which apps are in your installer's targets.
    // For example, AAP APK Installer targets AudioPluginServices (FIXME: it should also include hosts...).
    var findExistingPackages: (Context) -> List<String> = { listOf() }

    var isExistingPackageListReliable: () -> Boolean = { false }

    val logger = Logger()
}

object AppModelFactory {
    var create: () -> ApplicationModel = { TODO("It must be declared by each application") }
}
