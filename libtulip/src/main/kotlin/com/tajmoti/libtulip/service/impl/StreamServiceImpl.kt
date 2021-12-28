package com.tajmoti.libtulip.service.impl

import com.tajmoti.commonutils.combineNonEmpty
import com.tajmoti.commonutils.logger
import com.tajmoti.libtulip.model.info.StreamableInfo
import com.tajmoti.libtulip.model.key.StreamableKey
import com.tajmoti.libtulip.model.stream.StreamsResult
import com.tajmoti.libtulip.model.stream.UnloadedVideoStreamRef
import com.tajmoti.libtulip.repository.HostedTvDataRepository
import com.tajmoti.libtulip.repository.ItemMappingRepository
import com.tajmoti.libtulip.repository.getStreamableInfo
import com.tajmoti.libtulip.repository.getStreamableInfoByTmdbKey
import com.tajmoti.libtulip.service.StreamExtractionService
import com.tajmoti.libtulip.service.StreamService
import com.tajmoti.libtvprovider.model.VideoStreamRef
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class StreamServiceImpl(
    private val hostedTvDataRepository: HostedTvDataRepository,
    private val streamsRepo: StreamExtractionService,
    private val hostedToTmdbMappingRepository: ItemMappingRepository
) : StreamService {

    override fun getStreamsByKey(key: StreamableKey.Hosted): Flow<StreamsResult> {
        logger.debug("Retrieving $key")
        return hostedTvDataRepository.getStreamableInfo(key)
            .flatMapLatest { result -> result.fold(this::fetchStreamsForInfo) { flowOf(StreamsResult.Error) } }
    }

    private fun fetchStreamsForInfo(result: StreamableInfo.Hosted): Flow<StreamsResult> {
        return hostedTvDataRepository.fetchStreams(result.key)
            .map {
                it.toResult()
                    .map { streams -> streams.map { stream -> addMiscInfo(stream, result) } }
                    .map { streams -> sortByExtractionSupport(streams) }
                    .fold({ streams -> StreamsResult.Success(streams, true) }, { StreamsResult.Error })
            }
    }

    private fun addMiscInfo(stream: VideoStreamRef, info: StreamableInfo.Hosted): UnloadedVideoStreamRef {
        return UnloadedVideoStreamRef(stream, streamsRepo.canExtractStream(stream), info.language)
    }

    override fun getStreamsByKey(key: StreamableKey.Tmdb): Flow<StreamsResult> {
        logger.debug("Retrieving $key")
        return hostedTvDataRepository
            .getStreamableInfoByTmdbKey(hostedToTmdbMappingRepository, key)
            .flatMapLatest(::fetchStreamsForInfoResults)
    }

    private fun fetchStreamsForInfoResults(infos: List<Result<StreamableInfo.Hosted>>): Flow<StreamsResult> {
        return infos
            .map { result ->
                result.fold({ info -> getStreamsByKey(info.key) }, { flowOf(StreamsResult.Error) })
                    .onStart { emit(StreamsResult.Success(emptyList(), false)) }
                    .withIndex()
            }
            .combineNonEmpty()
            .filterNot { it.all { (index) -> index == 0 } } // Skips first useless emission caused by onStart
            .map { indexedResults ->
                val streams = indexedResults
                    .mapNotNull { (_, value) -> (value as? StreamsResult.Success)?.streams }
                    .flatten()
                    .let(::sortByExtractionSupport)
                // Skips the artificially emitted value in onStart, which is there to let us get here
                // before all flows from getStreamsByKey actually emit an initial value
                val allEmittedAtLeastOnce = indexedResults.all { (index) -> index > 0 }
                StreamsResult.Success(streams, allEmittedAtLeastOnce)
            }
    }

    private fun sortByExtractionSupport(videosWithLanguages: List<UnloadedVideoStreamRef>) =
        videosWithLanguages.sortedBy { vidWithLang -> !vidWithLang.linkExtractionSupported }
}