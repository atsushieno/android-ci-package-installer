package dev.atsushieno.cipackageinstaller

import android.content.Context
import org.kohsuke.github.GitHub
import org.kohsuke.github.GitHubBuilder
import org.kohsuke.github.connector.GitHubConnector
import java.util.Properties

class GitHubRepositoryStore(
    referrer: String
) : ApplicationStore(referrer) {

    data class GitHubCredentials(val username: String, val pat: String)

    override fun initialize(context: Context) {
        val creds = AppModel.getGitHubCredentials(context)
        if (creds.username.isNotEmpty() && creds.pat.isNotEmpty())
            updateCredentials(creds.username, creds.pat)
    }

    fun updateCredentials(username: String, pat: String) {
        github = GitHub.connect(username, pat)
    }

    var github: GitHub = GitHub.connectAnonymously()

    var githubRepositories = mutableListOf<RepositoryInformation>()

    override val repositories : List<RepositoryInformation>
        get() = githubRepositories
}
