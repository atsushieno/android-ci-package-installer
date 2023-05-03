package dev.atsushieno.cipackageinstaller

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.FileUtils
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

abstract class ApplicationStore(val referrer: String) {
    companion object {
        val empty = EmptyApplicationStore()
    }

    class EmptyApplicationStore : ApplicationStore("") {
        override val repositories: List<RepositoryInformation> = listOf()
        override fun initialize(context: Context) {}
    }

    class MergedApplicationStore(referrer: String) : ApplicationStore(referrer) {

        // Note that after call to initialize() on this class itself, any added store must be initialized before being added to this list.
        val stores = mutableListOf<ApplicationStore>()

        override val repositories: List<RepositoryInformation>
            get() = stores.flatMap { repositories }
        override fun initialize(context: Context) {
            // in principle this method should not initialize anything, as items in `stores` should have already been initialized.
        }
    }

    abstract val repositories: List<RepositoryInformation>
    abstract fun initialize(context: Context)
}

object AppModel {
    const val LOG_TAG: String = "CIPackageInstaller"
    private const val PENDING_INTENT_REQEST_CODE = 1

    private const val FILE_APK_PROVIDER_AUTHORITY_SUFFIX = ".fileprovider"
    private const val GITHUB_REPOSITORY_REFERRER = "https://github.com/atsushieno/android-ci-package-installer"

    private fun createSharedPreferences(context: Context) : SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(context, "AndroidCIPackageInstaller", masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
    }

    fun getGitHubCredentials(context: Context) : GitHubRepositoryStore.GitHubCredentials {
        val sp = createSharedPreferences(context)
        val user = sp.getString("GITHUB_USER", "") ?: ""
        val pat = sp.getString("GITHUB_PAT", "") ?: ""
        return GitHubRepositoryStore.GitHubCredentials(user, pat)
    }

    fun setGitHubCredentials(context: Context, username: String, pat: String) {
        val sp = createSharedPreferences(context)
        val edit = sp.edit()
        edit.putString("GITHUB_USER", username)
        edit.putString("GITHUB_PAT", pat)
        edit.apply()

        githubApplicationStore.updateCredentials(username, pat)
    }

    fun copyStream(inFS: InputStream, outFile: File) {
        val outFS = FileOutputStream(outFile)
        copyStream(inFS, outFS)
        outFS.close()
    }

    private fun copyStream(inFS: InputStream, outFS: OutputStream) {
        val bytes = ByteArray(4096)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            FileUtils.copy(inFS, outFS)
        } else {
            while (inFS.available() > 0) {
                val size = inFS.read(bytes)
                outFS.write(bytes, 0, size)
            }
        }
    }

    fun performInstallPackage(context: Context, download: ApplicationArtifact) {
        val repo = download.repository
        val installer = context.packageManager.packageInstaller

        val existing = installer.mySessions.firstOrNull { it.appPackageName == repo.info.packageName && !it.isActive }
        if (existing != null)
            // It seems better to abandon existing session and restart than throwing, when we perform install->uninstall->install...
            installer.openSession(existing.sessionId).abandon()
            //throw CIPackageInstallerException("Another operation for the package '${repo.info.packageName}' is in progress. Please wait for its completion.")

        val params = download.toPackageInstallerSessionParams()
        val sessionId = installer.createSession(params)
        val session = installer.openSession(sessionId)

        val file = download.downloadApp()
        val outStream = session.openWrite(file.name, 0, file.length())
        val inStream = FileInputStream(file)
        copyStream(inStream, outStream)
        session.fsync(outStream)
        outStream.close()

        val intent = Intent(context, PackageInstallerReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, PENDING_INTENT_REQEST_CODE,
            intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
        Log.d(LOG_TAG, "ready to install ${repo.info.appLabel} ...")
        session.commit(pendingIntent.intentSender)
        session.close()
    }
    fun performUninstallPackage(context: Context, repo: Repository) {
        val installer = context.packageManager.packageInstaller
        val intent = Intent(context, PackageInstallerReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, PENDING_INTENT_REQEST_CODE,
            intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
        installer.uninstall(repo.info.packageName, pendingIntent.intentSender)
    }

    // provide access to GitHub specific properties such as `guthubRepositories`
    val githubApplicationStore = GitHubRepositoryStore(GITHUB_REPOSITORY_REFERRER)

    var applicationStore: ApplicationStore = githubApplicationStore

    // This method is used to find the relevant packages that are already installed in an explicit way.
    // (We cannot simply query existing (installed) apps that exposes users privacy.)
    // Override it to determine which apps are in your installer's targets.
    // For example, AAP APK Installer targets AudioPluginServices (FIXME: it should also include hosts...).
    var findExistingPackages: (Context) -> List<String> = { listOf() }

    var isExistingPackageListReliable: () -> Boolean = { false }
}
