package com.tajmoti.libtulip.repository.impl

import arrow.core.Either
import com.tajmoti.commonutils.logger
import com.tajmoti.libtulip.repository.StreamsRepository
import com.tajmoti.libtvprovider.VideoStreamRef
import com.tajmoti.libtvvideoextractor.ExtractionError
import com.tajmoti.libtvvideoextractor.VideoLinkExtractor
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import java.net.URI

class StreamsRepositoryImpl(
    private val linkExtractor: VideoLinkExtractor,
    private val httpClient: HttpClient
) : StreamsRepository {

    override fun canExtractFromService(ref: VideoStreamRef): Boolean {
        return linkExtractor.canExtractUrl(ref.url)
                || linkExtractor.canExtractService(ref.serviceName)
    }

    override suspend fun resolveStream(
        ref: VideoStreamRef.Unresolved
    ): Result<VideoStreamRef.Resolved> {
        // If there were no redirects, assume that the link already points to the streaming page
        return resolveRedirects(ref.url)
            .onFailure { logger.warn("Failed to resolve redirects of $ref", it) }
            .map { ref.asResolved(it ?: ref.url) }
    }

    /**
     * Performs a GET request to the [url] and returns the value of the `location` response header.
     * If the header is missing, returns null.
     */
    private suspend fun resolveRedirects(url: String): Result<String?> {
        return runCatching {
            val originalHost = URI.create(url).host
            logger.debug("Resolving redirects of '$url'")
            var nextLocation = url
            var attempts = 0
            while (attempts++ < MAX_REDIRECTS && shouldRetryRedirect(nextLocation, originalHost)) {
                val response: HttpResponse = httpClient.request(nextLocation)
                nextLocation = response.headers["location"] ?: return@runCatching nextLocation
                logger.debug("Next location for '$url' is '$nextLocation'")
            }
            logger.debug("Redirects of '$url' resolved into '$nextLocation'")
            nextLocation
        }
    }

    private fun shouldRetryRedirect(nextLocation: String, originalHost: String): Boolean {
        val nextHost = URI.create(nextLocation).host
        return nextHost.contains(originalHost) || originalHost.contains(nextHost)
    }

    override suspend fun extractVideoLink(info: VideoStreamRef.Resolved):
            Either<ExtractionError, String> {
        return linkExtractor.extractVideoLink(info.url, info.serviceName)
    }

    companion object {
        /**
         * How many redirects are attempted before returning in [resolveRedirects]
         */
        private const val MAX_REDIRECTS = 10
    }
}