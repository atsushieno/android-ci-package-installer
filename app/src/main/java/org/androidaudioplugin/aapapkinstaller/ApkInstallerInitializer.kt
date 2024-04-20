package org.androidaudioplugin.aapapkinstaller

import android.content.Context
import androidx.startup.Initializer
import dev.atsushieno.cipackageinstaller.AppModelFactory
import dev.atsushieno.cipackageinstaller.ApplicationModel

class ApkInstallerInitializer : Initializer<Any> {
    override fun create(context: Context) {
        AppModelFactory.create = {
            object : ApplicationModel() {
                // They are specific to my app.
                // Replace them with your own if you want to reuse cipackageinstaller package as a library.
                override val LOG_TAG = "AAPAPKInstaller"
                override val installerSessionReferrer =
                    "https://github.com/atsushieno/android-ci-package-installer"
            }
        }
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf()
}