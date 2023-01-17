package dev.atsushieno.cipackageinstaller

import android.content.pm.PackageInstaller
import android.graphics.Bitmap
import java.io.File

abstract class RepositoryInformation {
    abstract val name: String
    abstract val icon: Bitmap?
    abstract val appLabel: String
    abstract val packageName: String

    abstract fun createRepository() : Repository
}

abstract class Repository(open val info: RepositoryInformation) {
    abstract fun toPackageInstallerSessionParams() : PackageInstaller.SessionParams

    abstract fun downloadApp(): File

    abstract val appName: String
    abstract val versionId: String
}