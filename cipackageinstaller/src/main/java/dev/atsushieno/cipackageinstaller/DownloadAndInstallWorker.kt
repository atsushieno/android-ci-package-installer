package dev.atsushieno.cipackageinstaller

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.icu.util.ULocale
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.FileInputStream

class DownloadAndInstallWorker(context: Context, parameters: WorkerParameters)
    : Worker(context, parameters) {
    companion object {
        const val WORK_NAME = "CIPackageInstallWorker"
        const val INPUT_DATA_ARTIFACT_TYPE = "artifactType"
        const val INPUT_DATA_DOWNLOAD = "download"
        private const val PENDING_INTENT_REQUEST_CODE = 1
        private const val PENDING_PREAPPROVAL_REQUEST_CODE = 2

        val LOG_TAG by AppModel::LOG_TAG
    }

    private val download: ApplicationArtifact by lazy {
        val inputData = parameters.inputData
        val artifactType = inputData.getString(INPUT_DATA_ARTIFACT_TYPE)
            ?: throw CIPackageInstallerException("Internal error: missing INPUT_DATA_ARTIFACT_TYPE")
        val source = inputData.getString(INPUT_DATA_DOWNLOAD)
            ?: throw CIPackageInstallerException("Internal error: missing INPUT_DATA_DOWNLOAD")
        ApplicationArtifactFactory.deserialize(artifactType, source)
    }

    override fun doWork(): Result {
        try {
            downloadAndInstall()
            return Result.success()
        } catch (ex: Exception) {
            Log.e(LOG_TAG, ex.toString())
            return Result.failure()
        }
    }

    private fun downloadAndInstall() {
        val context = applicationContext
        val repo = download.repository
        val installer = context.packageManager.packageInstaller

        Log.d(LOG_TAG, "InstallWorker started for ${repo.info.appLabel} ...")

        val existing = installer.mySessions.firstOrNull { it.appPackageName == repo.info.packageName && !it.isActive }
        if (existing != null)
        // abandon existing session and restart than throwing, when we perform install->uninstall->install...
            installer.openSession(existing.sessionId).abandon()

        val params = repo.getPackageInstallerSessionParams(download)
        val sessionId = installer.createSession(params)
        val session = installer.openSession(sessionId)

        // Pre-approval is available only in Android 14 or later.
        if (AppModel.preApprovalEnabled) {
            val preapprovalIntent = Intent(context, PreapprovalReceiver::class.java)
            val preapprovalPendingIntent = PendingIntent.getBroadcast(context,
                PENDING_PREAPPROVAL_REQUEST_CODE, preapprovalIntent, PendingIntent.FLAG_MUTABLE)
            val preapproval = PackageInstaller.PreapprovalDetails.Builder()
                .setPackageName(repo.info.packageName)
                .setLabel(repo.info.appLabel)
                .setLocale(ULocale.getDefault())
                .build()
            session.requestUserPreapproval(preapproval, preapprovalPendingIntent.intentSender)
        }

        Log.d(LOG_TAG, "start downloading ${repo.info.appLabel} ...")

        setForegroundAsync(DownloadStatusNotificationManager.createForegroundInfo(context))
        val file = download.downloadApp()
        Log.d(LOG_TAG, "completed downloading ${repo.info.appLabel}")
        val outStream = session.openWrite(file.name, 0, file.length())
        val inStream = FileInputStream(file)
        AppModel.copyStream(inStream, outStream)
        session.fsync(outStream)
        outStream.close()

        val intent = Intent(context, PackageInstallerReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, PENDING_INTENT_REQUEST_CODE,
            intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
        Log.d(LOG_TAG, "ready to install ${repo.info.appLabel} ...")
        session.commit(pendingIntent.intentSender)
        session.close()

        Log.d(LOG_TAG, "InstallWorker completed for ${repo.info.appLabel}")
    }
}
