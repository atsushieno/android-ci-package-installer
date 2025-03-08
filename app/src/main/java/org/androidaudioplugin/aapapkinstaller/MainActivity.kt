package org.androidaudioplugin.aapapkinstaller

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.core.graphics.drawable.IconCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import dev.atsushieno.cipackageinstaller.AppModel
import dev.atsushieno.cipackageinstaller.AppModelFactory
import dev.atsushieno.cipackageinstaller.ApplicationModel
import dev.atsushieno.cipackageinstaller.ui.view.CIPackageInstallerActivity
import dev.atsushieno.cipackageinstaller.GitHubRepositoryInformation

class MainActivity : CIPackageInstallerActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        notificationIcon = createNotificationIcon()
        ApplicationCatalog.setupApkList()
        AppModel.applicationStore.initialize(this)
        // FIXME: this should include host app packages by some means
        AppModel.findExistingPackages = { context -> queryInstalledAudioPluginPackages(context) }
    }

    override fun createNotificationIcon(): IconCompat =
        IconCompat.createWithResource(this, R.drawable.ic_launcher_foreground)

    private fun queryInstalledAudioPluginPackages(context: Context, packageNameFilter: String? = null): List<String> {
        val AAP_ACTION_NAME = "org.androidaudioplugin.AudioPluginService.V3"

        val intent = Intent(AAP_ACTION_NAME)
        if (packageNameFilter != null)
            intent.setPackage(packageNameFilter)
        return context.packageManager.queryIntentServices(intent, 0).map { it.serviceInfo.packageName }
            .distinct()
    }
}