package dev.atsushieno.cipackageinstaller

import android.content.Context
import android.util.Log
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.apache.commons.io.FileUtils
import org.kohsuke.github.GitHub
import org.kohsuke.github.GitHubBuilder
import org.kohsuke.github.extras.okhttp3.OkHttpGitHubConnector


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
        try {
            github = GitHubBuilder.fromEnvironment()
                .withOAuthToken(pat.trim(), username.trim())
                .withConnector(
                    OkHttpGitHubConnector(
                        OkHttpClient().newBuilder().cache(cache).build()
                    )
                )
                .build()
        } catch (e: Exception) {
            // keep using current github connection
            // FIXME: there should be some way to notify user that the authentication token is invalid.
            Log.e(AppModel.LOG_TAG, "GitHub authentication failed. Error details are being dumped.")
            Log.e(AppModel.LOG_TAG, e.toString())
        }
    }

    private val cache = Cache(FileUtils.getTempDirectory(), (100 * 1024 * 1024).toLong()) // 100MB cache

    private var gh: GitHub? = null
    var github: GitHub
        get() {
            gh = gh ?: connectGitHub()
            return gh!!
        }
        set(value) { gh = value }

    private fun connectGitHub() = GitHubBuilder.fromEnvironment()
        .withConnector(OkHttpGitHubConnector(OkHttpClient().newBuilder().cache(cache).build()))
        .build()

    var githubRepositories = mutableListOf<RepositoryInformation>()

    override val repositories : List<RepositoryInformation>
        get() = githubRepositories
}
