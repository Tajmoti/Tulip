package com.tajmoti.libtulip.service.impl

import com.tajmoti.commonutils.logger
import com.tajmoti.libtulip.model.hosted.StreamingService
import com.tajmoti.libtulip.model.stream.UnloadedVideoStreamRef
import com.tajmoti.libtulip.service.StreamExtractorService
import com.tajmoti.libtvprovider.MultiTvProvider
import com.tajmoti.libtvprovider.VideoStreamRef
import com.tajmoti.libtvvideoextractor.VideoLinkExtractor
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import javax.inject.Inject

class StreamsExtractionServiceImpl @Inject constructor(
    private val linkExtractor: VideoLinkExtractor,
    private val httpClient: HttpClient,
    private val tvProvider: MultiTvProvider<StreamingService>
) : StreamExtractorService {

    override suspend fun fetchStreams(
        service: StreamingService,
        streamableKey: String
    ): Result<List<UnloadedVideoStreamRef>> {
        val result = tvProvider.getStreamableLinks(service, streamableKey).getOrElse {
            return Result.failure(it)
        }
        val sorted = result.map { UnloadedVideoStreamRef(it, canExtractFromService(it)) }
        return Result.success(sorted)
    }

    private fun canExtractFromService(ref: VideoStreamRef): Boolean {
        return linkExtractor.canExtractUrl(ref.url)
                || linkExtractor.canExtractService(ref.serviceName)
    }

    override suspend fun resolveStream(ref: VideoStreamRef.Unresolved): Result<VideoStreamRef.Resolved> {
        val realUrl = resolveRedirects(ref.url)
            .getOrElse { return Result.failure(it) }
        // If there were no redirects, assume that the link already points to the streaming page
        return Result.success(ref.asResolved(realUrl ?: ref.url))
    }

    /**
     * Performs a GET request to the [url] and returns the value of the `location` response header.
     * If the header is missing, returns null.
     */
    private suspend fun resolveRedirects(url: String): Result<String?> {
        return runCatching {
            val response: HttpResponse = httpClient.request(url)
            response.headers["location"]
        }
    }

    override suspend fun extractVideoLink(info: VideoStreamRef.Resolved): Result<String> {
        return linkExtractor.extractVideoLink(info.url, info.serviceName)
            .onFailure { logger.warn("Link extraction for $info failed!", it) }
    }
}