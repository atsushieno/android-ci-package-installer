package dev.atsushieno.cipackageinstaller

import kotlinx.serialization.json.Json
import java.io.File

abstract class ApplicationArtifact(open val repository: Repository) {
    abstract val articactInfoType: String
    abstract fun serializeToString(): String
    abstract fun downloadApp(): File

    abstract val typeName: String
    abstract val artifactName: String
    abstract val versionId: String
    abstract val artifactSizeInBytes: Long
}

object ApplicationArtifactFactory {
    private val factories = mutableMapOf<String,(String)->ApplicationArtifact>()

    fun register(type: String, factory: (String)->ApplicationArtifact) = factories.put(type, factory)

    fun deserialize(type: String, data: String): ApplicationArtifact {
        val f = factories[type] ?: throw CIPackageInstallerException("Internal error: missing ApplicationArtifact factory")
        return f.invoke(data)
    }

    init {
        // Register known factories
        register(GitHubLatestWorkflowRunArtifactInfo::class.java.name) { data ->
            val artifactInfo = Json.decodeFromString<GitHubLatestWorkflowRunArtifactInfo>(data)
            // We need to revamp here
            val repoInfo = GitHubRepositoryInformation(artifactInfo.account, artifactInfo.repository, artifactInfo.packageName, artifactInfo.appLabel)
            return@register GitHubArtifactApplicationArtifact.tryCreate(repoInfo.createRepository() as GitHubRepository) ?: throw CIPackageInstallerException("Failed to deserialize GitHubArtifactApplicationArtifact")
        }
        register(GitHubReleaseApplicationArtifactInfo::class.java.name) { data ->
            val artifactInfo = Json.decodeFromString<GitHubReleaseApplicationArtifactInfo>(data)
            val repoInfo = GitHubRepositoryInformation(artifactInfo.account, artifactInfo.repository, artifactInfo.packageName, artifactInfo.appLabel)
            val repo = repoInfo.createRepository() as GitHubRepository
            return@register GitHubReleaseApplicationArtifact.tryCreate(repo, artifactInfo.releaseTag)
        }
    }
}
