package com.tajmoti.libtulip.service.impl

import com.tajmoti.libtulip.model.StreamableInfo
import com.tajmoti.libtulip.model.StreamableInfoWithLinks
import com.tajmoti.libtulip.model.UnloadedVideoStreamRef
import com.tajmoti.libtulip.service.StreamExtractorService
import com.tajmoti.libtvprovider.VideoStreamRef
import com.tajmoti.libtvvideoextractor.VideoLinkExtractor
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import javax.inject.Inject

class StreamsExtractionServiceImpl @Inject constructor(
    private val linkExtractor: VideoLinkExtractor,
    private val httpClient: HttpClient
) : StreamExtractorService {

    override suspend fun fetchStreams(streamable: StreamableInfo): Result<StreamableInfoWithLinks> {
        val result = streamable.streamable.loadSources().getOrElse {
            return Result.failure(it)
        }
        val sorted = mapAndSortLinksByRelevance(result)
        return Result.success(StreamableInfoWithLinks(streamable, sorted))
    }

    private fun mapAndSortLinksByRelevance(it: List<VideoStreamRef>): List<UnloadedVideoStreamRef> {
        val links = it.map { UnloadedVideoStreamRef(it, canExtractFromService(it)) }
        val extractable = links.filter { it.linkExtractionSupported }
        val notExtractable = links.filterNot { it.linkExtractionSupported }
        return extractable + notExtractable
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
    }
}