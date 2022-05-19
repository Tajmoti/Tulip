package com.tajmoti.libtulip.facade

import arrow.core.Either
import arrow.core.left
import com.tajmoti.commonutils.combineNonEmpty
import com.tajmoti.commonutils.logger
import com.tajmoti.libtulip.dto.CaptchaInfoDto
import com.tajmoti.libtulip.dto.ExtractionErrorDto
import com.tajmoti.libtulip.dto.StreamListDto
import com.tajmoti.libtulip.dto.StreamingSiteLinkDto
import com.tajmoti.libtulip.model.info.StreamableInfo
import com.tajmoti.libtulip.model.key.StreamableKey
import com.tajmoti.libtulip.model.result.toResult
import com.tajmoti.libtulip.service.HostedTvDataRepository
import com.tajmoti.libtulip.service.ItemMappingRepository
import com.tajmoti.libtulip.service.getStreamableInfo
import com.tajmoti.libtulip.service.getStreamableInfoByTmdbKey
import com.tajmoti.libtulip.util.RedirectResolver
import com.tajmoti.libtvprovider.model.StreamingSiteLink
import com.tajmoti.libtvvideoextractor.ExtractionError
import com.tajmoti.libtvvideoextractor.VideoLinkExtractor
import io.ktor.client.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalCoroutinesApi::class)
class StreamFacadeImpl(
    private val hostedTvDataRepository: HostedTvDataRepository,
    private val hostedToTmdbMappingRepository: ItemMappingRepository,
    private val linkExtractor: VideoLinkExtractor,
    httpClient: HttpClient
) : StreamFacade {
    private val redirectResolver = RedirectResolver(httpClient)


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

    private fun addMiscInfo(stream: StreamingSiteLink, info: StreamableInfo.Hosted): StreamingSiteLinkDto {
        return StreamingSiteLinkDto(stream.serviceName, stream.url, canExtractStream(stream), info.language)
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

    private fun sortByExtractionSupport(videosWithLanguages: List<StreamingSiteLinkDto>) =
        videosWithLanguages.sortedBy { vidWithLang -> !vidWithLang.linkExtractionSupported }


    private fun canExtractStream(ref: StreamingSiteLink): Boolean {
        return linkExtractor.canExtractUrl(ref.url)
                || linkExtractor.canExtractService(ref.serviceName)
    }

    override suspend fun extractVideoLink(ref: StreamingSiteLinkDto): Either<ExtractionErrorDto, String> {
        val resolved = redirectResolver.resolveRedirects(ref.url)
            .onFailure { logger.warn(it) { "Failed to resolve redirects of $ref" } }
            .getOrElse { return ExtractionErrorDto.Exception(it).left() }
        return linkExtractor.extractVideoLink(resolved ?: ref.url, ref.serviceName)
            .bimap({ it.mapExtractionError() }, { it })
            .tapLeft { logger.warn { "Extraction of $ref failed with $it" } }
    }

    private fun ExtractionError.mapExtractionError(): ExtractionErrorDto {
        return when (this) {
            is ExtractionError.Captcha ->
                ExtractionErrorDto.Captcha(CaptchaInfoDto(info.captchaUrl, info.destinationUrl))
            is ExtractionError.Exception ->
                ExtractionErrorDto.Exception(throwable)
            is ExtractionError.NoHandler ->
                ExtractionErrorDto.NoHandler
            is ExtractionError.UnexpectedPageContent ->
                ExtractionErrorDto.UnexpectedPageContent
        }
    }
}