package dev.atsushieno.cipackageinstaller

import android.content.pm.PackageInstaller
import android.graphics.Bitmap

abstract class RepositoryInformation {
    abstract val name: String
    abstract val icon: Bitmap?
    abstract val appLabel: String
    abstract val packageName: String

    abstract fun createRepository() : Repository
}

abstract class Repository(open val info: RepositoryInformation) {
    abstract val variants: List<ApplicationArtifact>

    abstract fun getPackageInstallerSessionParams(artifact: ApplicationArtifact) : PackageInstaller.SessionParams
}

