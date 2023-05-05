package dev.atsushieno.cipackageinstaller

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
import android.util.Log
import java.lang.RuntimeException

class PackageInstallerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -37564)) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                if (AppModel.preApprovalEnabled) {
                    // do nothing, we may be still presenting the preapproval dialog
                } else {
                    val i = intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
                        ?: throw java.lang.IllegalStateException("No extra intent found")
                    context.startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            }
            PackageInstaller.STATUS_SUCCESS -> {
                Log.d(AppModel.LOG_TAG, "Installation succeeded!")
            }
            else -> {
                val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
                Log.e(AppModel.LOG_TAG, "Installation result: $message")
                val sessionId = intent.getIntExtra(PackageInstaller.EXTRA_SESSION_ID, 0)
                if (sessionId > 0) {
                    try {
                        // Some sample apps tell it should be disposed here,
                        // but as far as I observed it is automatically disposed.
                        // So, do it conditionally, in case if it is not done...
                        if (context.packageManager.packageInstaller.getSessionInfo(sessionId) != null)
                            context.packageManager.packageInstaller.abandonSession(sessionId)
                    } catch (ex: RuntimeException) {
                        // otherwise, "java.lang.SecurityException: Caller has no access to session"
                        Log.e(AppModel.LOG_TAG,
                            "Attempt to abandon the session $sessionId caused an exception: $ex.message"
                        )
                    }
                }
            }
        }
    }
}