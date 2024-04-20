package dev.atsushieno.cipackageinstaller

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.content.pm.ShortcutInfo
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.ForegroundInfo
import dev.atsushieno.cipackageinstaller.ui.view.CIPackageInstallerActivity
import kotlin.random.Random

object DownloadStatusNotificationManager {
    private const val bubbleRequestCode = 0 // FIXME: manage them
    private const val bubbleShortLabel = "FIXME-shortLabel"
    private const val mainRequestCode = 1 // FIXME: manage them
    private const val category = "FIXME-com.example.category.IMG_SHARE_TARGET"
    private val notificationId = Random.nextInt() // FIXME: manage them
    val shortcutId = "FIXME-shortcutId"

    fun createForegroundInfo(context: Context): ForegroundInfo {
        val notificationChannelId = javaClass.name

        val channel = NotificationChannelCompat.Builder(
            notificationChannelId,
            NotificationManagerCompat.IMPORTANCE_DEFAULT
        ).apply {
            setName("DownloadStatusNotificationService")
        }.build()
        val manager = NotificationManagerCompat.from(context)
        manager.createNotificationChannel(channel)

        val target = Intent(context, CIPackageInstallerActivity.mainActivityClass)
        val bubbleIntent =
            PendingIntent.getActivity(context, bubbleRequestCode, target, PendingIntent.FLAG_IMMUTABLE)

        // Create a sharing shortcut.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val shortcutBuilder = ShortcutInfo.Builder(context, shortcutId)
                .setCategories(setOf(category))
                .setIntent(Intent(Intent.ACTION_DEFAULT))
                .setShortLabel(bubbleShortLabel)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                shortcutBuilder.setLongLived(true)
            }
            shortcutBuilder.build()
        }

        // Create a bubble metadata.
        val bubbleData = NotificationCompat.BubbleMetadata.Builder(
            bubbleIntent,
            CIPackageInstallerActivity.notificationIcon
        )
            .setDesiredHeight(600)
            .build()

        // Create a notification, referencing the sharing shortcut.
        val builder = NotificationCompat.Builder(context, notificationChannelId)
            .setBubbleMetadata(bubbleData)
            .setShortcutId(shortcutId)
            .setOnlyAlertOnce(true)
            .setContentInfo("TEST")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            builder.setSmallIcon(CIPackageInstallerActivity.notificationIcon)
        val notification = builder.build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            ForegroundInfo(notificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        else
            ForegroundInfo(notificationId, notification)
    }
}