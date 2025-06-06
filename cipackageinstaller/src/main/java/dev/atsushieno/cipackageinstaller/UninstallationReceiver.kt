package dev.atsushieno.cipackageinstaller

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.util.Log
import androidx.annotation.RequiresApi

class UninstallationReceiver : BroadcastReceiver() {
    @RequiresApi(34)
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -37564)) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                val i = intent.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
                    ?: throw java.lang.IllegalStateException("No extra intent found")
                context.startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
            PackageInstaller.STATUS_SUCCESS -> {
                Log.d(AppModel.LOG_TAG, "Successfully uninstalled")
            }
            else -> {
                val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
                AppModel.logger.logError("Uninstallation failed: $message")
            }
        }
    }
}
