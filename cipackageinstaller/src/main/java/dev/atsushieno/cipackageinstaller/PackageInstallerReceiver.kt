package dev.atsushieno.cipackageinstaller

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.util.Log
import java.lang.RuntimeException

class PackageInstallerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -1)) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                val i = intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
                    ?: throw java.lang.IllegalStateException("No extra intent found")
                context.startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
            PackageInstaller.STATUS_SUCCESS -> {
                Log.d(AppModel.LOG_TAG, "Installation succeeded!")
            }
            else -> {
                val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
                Log.e(AppModel.LOG_TAG, "Installation result: $message")
                val sessionId = intent.getIntExtra(PackageInstaller.EXTRA_SESSION_ID, 0)
                if (sessionId > 0)
                    try {
                        context.packageManager.packageInstaller.abandonSession(sessionId)
                    } catch (ex: RuntimeException) {
                        // otherwise, "java.lang.SecurityException: Caller has no access to session"
                    }
            }
        }
    }
}