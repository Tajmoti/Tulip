package com.tajmoti.libtulip.util

import com.tajmoti.commonutils.logger
import com.tajmoti.multiplatform.Uri
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

class RedirectResolver(
    private val httpClient: HttpClient
) {
    companion object {
        /**
         * How many redirects are attempted before returning in [resolveRedirects]
         */
        private const val MAX_REDIRECTS = 10
    }


    /**
     * Performs a GET request to the [url] and returns the value of the `location` response header.
     * If the header is missing, returns null.
     */
    suspend fun resolveRedirects(url: String): Result<String?> {
        return runCatching {
            val originalHost = Uri(url).host
            logger.debug { "Resolving redirects of '$url'" }
            var nextLocation = url
            var attempts = 0
            while (attempts++ < MAX_REDIRECTS && shouldRetryRedirect(nextLocation, originalHost)) {
                val response: HttpResponse = httpClient.request(nextLocation)
                nextLocation = response.headers["location"] ?: return@runCatching nextLocation
                logger.debug { "Next location for '$url' is '$nextLocation'" }
            }
            logger.debug { "Redirects of '$url' resolved into '$nextLocation'" }
            nextLocation
        }
    }

    private fun shouldRetryRedirect(nextLocation: String, originalHost: String): Boolean {
        val nextHost = Uri(nextLocation).host
        return nextHost.contains(originalHost) || originalHost.contains(nextHost)
    }
}