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
    abstract val variants: List<ApplicationArtifact>
}

abstract class ApplicationArtifact(open val repository: Repository) {
    abstract fun toPackageInstallerSessionParams() : PackageInstaller.SessionParams

    abstract fun downloadApp(): File

    abstract val typeName: String
    abstract val artifactName: String
    abstract val versionId: String
    abstract val artifactSizeInBytes: Long
}