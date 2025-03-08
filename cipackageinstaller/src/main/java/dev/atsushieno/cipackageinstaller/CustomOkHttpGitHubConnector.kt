// It is based on https://github.com/hub4j/github-api/blob/04af6e38/src/main/java/org/kohsuke/github/extras/okhttp3/OkHttpGitHubConnector.java

package dev.atsushieno.cipackageinstaller

import okhttp3.CacheControl
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.apache.commons.io.IOUtils
import org.kohsuke.github.connector.GitHubConnector
import org.kohsuke.github.connector.GitHubConnectorRequest
import org.kohsuke.github.connector.GitHubConnectorResponse
import org.kohsuke.github.connector.GitHubConnectorResponse.ByteArrayResponse
import java.io.InputStream
import java.util.Arrays
import java.util.concurrent.TimeUnit

class CustomOkHttpGitHubConnector(client: OkHttpClient, cacheMaxAge: Int = 0) : GitHubConnector {
    private var maxAgeHeaderValue: String? = null

    private val client: OkHttpClient

    init {
        val builder = client.newBuilder()

        builder.connectionSpecs(TlsConnectionSpecs())
        this.client = builder.build()
        maxAgeHeaderValue =
            if (cacheMaxAge >= 0 && this.client != null && this.client.cache != null) {
                CacheControl.Builder().maxAge(cacheMaxAge, TimeUnit.SECONDS).build().toString()
            } else {
                null
            }
    }

    override fun send(request: GitHubConnectorRequest): GitHubConnectorResponse {
        val builder = Request.Builder().url(request.url())
        if (maxAgeHeaderValue != null && request.header(HEADER_NAME) == null) {
            builder.header(HEADER_NAME, maxAgeHeaderValue!!)
        }

        for ((key, v) in request.allHeaders()) {
            if (v != null) {
                builder.addHeader(key!!, java.lang.String.join(", ", v))
            }
        }

        var body: RequestBody? = null
        if (request.hasBody()) {
            body = RequestBody.create(null, IOUtils.toByteArray(request.body()))
        }
        builder.method(request.method(), body)
        val okhttpRequest = builder.build()
        val okhttpResponse = client.newCall(okhttpRequest).execute()

        return CustomOkHttpGitHubConnectorResponse(request, okhttpResponse)
    }

    private fun TlsConnectionSpecs(): List<ConnectionSpec> {
        return Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.CLEARTEXT)
    }

    private class CustomOkHttpGitHubConnectorResponse(
        request: GitHubConnectorRequest?,
        private val response: Response
    ) : ByteArrayResponse(request!!, response.code, response.headers.toMultimap()) {

        val wrappedStream by lazy {
            synchronized(this) {
                val rawStream = rawBodyStream()
                wrapStream(rawStream)
            }
        }

        // It is customized point. We do not want huge bytearray
        override fun bodyStream(): InputStream {
            return wrappedStream
        }

        override fun rawBodyStream(): InputStream? {
            val body = response.body
            return body?.byteStream()
        }

        override fun close() {
            super.close()
            response.close()
        }
    }

    companion object {
        private const val HEADER_NAME = "Cache-Control"
    }
}
