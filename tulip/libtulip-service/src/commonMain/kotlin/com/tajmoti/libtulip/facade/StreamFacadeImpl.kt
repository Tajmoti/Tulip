package com.tajmoti.libtulip.facade

import arrow.core.Either
import com.tajmoti.commonutils.combineNonEmpty
import com.tajmoti.commonutils.logger
import com.tajmoti.libtulip.model.info.StreamableInfo
import com.tajmoti.libtulip.model.key.StreamableKey
import com.tajmoti.libtulip.model.result.toResult
import com.tajmoti.libtulip.dto.StreamListDto
import com.tajmoti.libtulip.model.stream.UnloadedVideoStreamRef
import com.tajmoti.libtulip.service.HostedTvDataRepository
import com.tajmoti.libtulip.service.ItemMappingRepository
import com.tajmoti.libtulip.service.getStreamableInfo
import com.tajmoti.libtulip.service.getStreamableInfoByTmdbKey
import com.tajmoti.libtvprovider.model.VideoStreamRef
import com.tajmoti.libtvvideoextractor.ExtractionError
import com.tajmoti.libtvvideoextractor.VideoLinkExtractor
import com.tajmoti.multiplatform.Uri
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalCoroutinesApi::class)
class StreamFacadeImpl(
    private val hostedTvDataRepository: HostedTvDataRepository,
    private val hostedToTmdbMappingRepository: ItemMappingRepository,
    private val linkExtractor: VideoLinkExtractor,
    private val httpClient: HttpClient
) : StreamFacade {

    override fun getStreamsByKey(key: StreamableKey.Hosted): Flow<StreamListDto> {
        logger.debug { "Retrieving streams by $key" }
        return hostedTvDataRepository.getStreamableInfo(key)
            .flatMapLatest { result -> result.fold(this::fetchStreamsForInfo) { flowOf(StreamListDto.Error) } }
    }

    private fun fetchStreamsForInfo(result: StreamableInfo.Hosted): Flow<StreamListDto> {
        return hostedTvDataRepository.fetchStreams(result.key)
            .map {
                it.toResult()
                    .map { streams -> streams.map { stream -> addMiscInfo(stream, result) } }
                    .map { streams -> sortByExtractionSupport(streams) }
                    .fold({ streams -> StreamListDto.Success(streams, true) }, { StreamListDto.Error })
            }
    }

    private fun addMiscInfo(stream: VideoStreamRef, info: StreamableInfo.Hosted): UnloadedVideoStreamRef {
        return UnloadedVideoStreamRef(stream, canExtractStream(stream), info.language)
    }

    override fun getStreamsByKey(key: StreamableKey.Tmdb): Flow<StreamListDto> {
        logger.debug { "Retrieving streams by $key" }
        return hostedTvDataRepository
            .getStreamableInfoByTmdbKey(hostedToTmdbMappingRepository, key)
            .flatMapLatest(::fetchStreamsForInfoResults)
    }

    private fun fetchStreamsForInfoResults(infos: List<Result<StreamableInfo.Hosted>>): Flow<StreamListDto> {
        return infos
            .map { result ->
                result.fold({ info -> getStreamsByKey(info.key) }, { flowOf(StreamListDto.Error) })
                    .onStart { emit(StreamListDto.Success(emptyList(), false)) }
                    .withIndex()
            }
            .combineNonEmpty()
            .filterNot { it.all { (index) -> index == 0 } } // Skips first useless emission caused by onStart
            .map { indexedResults ->
                val streams = indexedResults
                    .mapNotNull { (_, value) -> (value as? StreamListDto.Success)?.streams }
                    .flatten()
                    .let(::sortByExtractionSupport)
                // Skips the artificially emitted value in onStart, which is there to let us get here
                // before all flows from getStreamsByKey actually emit an initial value
                val allEmittedAtLeastOnce = indexedResults.all { (index) -> index > 0 }
                StreamListDto.Success(streams, allEmittedAtLeastOnce)
            }
    }

    private fun sortByExtractionSupport(videosWithLanguages: List<UnloadedVideoStreamRef>) =
        videosWithLanguages.sortedBy { vidWithLang -> !vidWithLang.linkExtractionSupported }


    override fun canExtractStream(ref: VideoStreamRef): Boolean {
        return linkExtractor.canExtractUrl(ref.url)
                || linkExtractor.canExtractService(ref.serviceName)
    }

    override suspend fun resolveStream(
        ref: VideoStreamRef.Unresolved
    ): Result<VideoStreamRef.Resolved> {
        // If there were no redirects, assume that the link already points to the streaming page
        return resolveRedirects(ref.url)
            .onFailure { logger.warn(it) { "Failed to resolve redirects of $ref" } }
            .map { ref.asResolved(it ?: ref.url) }
    }

    private fun VideoStreamRef.Unresolved.asResolved(resolvedUrl: String): VideoStreamRef.Resolved {
        return VideoStreamRef.Resolved(serviceName, resolvedUrl, this)
    }

    /**
     * Performs a GET request to the [url] and returns the value of the `location` response header.
     * If the header is missing, returns null.
     */
    private suspend fun resolveRedirects(url: String): Result<String?> {
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