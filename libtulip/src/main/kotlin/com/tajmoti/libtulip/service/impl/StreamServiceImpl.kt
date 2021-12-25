package com.tajmoti.libtulip.service.impl

import com.tajmoti.commonutils.combine
import com.tajmoti.libtulip.model.info.StreamableInfo
import com.tajmoti.libtulip.model.key.StreamableKey
import com.tajmoti.libtulip.model.stream.UnloadedVideoStreamRef
import com.tajmoti.libtulip.repository.HostedTvDataRepository
import com.tajmoti.libtulip.repository.ItemMappingRepository
import com.tajmoti.libtulip.repository.getStreamableInfo
import com.tajmoti.libtulip.repository.getStreamableInfoByTmdbKey
import com.tajmoti.libtulip.service.StreamExtractionService
import com.tajmoti.libtulip.service.StreamService
import com.tajmoti.libtvprovider.VideoStreamRef
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class StreamServiceImpl(
    private val hostedTvDataRepository: HostedTvDataRepository,
    private val streamsRepo: StreamExtractionService,
    private val hostedToTmdbMappingRepository: ItemMappingRepository
) : StreamService {

    override fun getStreamsByKey(key: StreamableKey.Hosted): Flow<Result<List<UnloadedVideoStreamRef>>> {
        return hostedTvDataRepository.getStreamableInfo(key)
            .flatMapLatest { result -> result.fold(this::fetchStreamsForInfo) { flowOf(Result.failure(it)) } }
    }

    private fun fetchStreamsForInfo(result: StreamableInfo.Hosted): Flow<Result<List<UnloadedVideoStreamRef>>> {
        return hostedTvDataRepository.fetchStreams(result.key)
            .map {
                it.toResult()
                    .map { streams -> streams.map { stream -> addMiscInfo(stream, result) } }
                    .map { streams -> sortByExtractionSupport(streams) }
            }
    }

    private fun addMiscInfo(stream: VideoStreamRef, info: StreamableInfo.Hosted): UnloadedVideoStreamRef {
        return UnloadedVideoStreamRef(stream, streamsRepo.canExtractStream(stream), info.language)
    }

    override fun getStreamsByKey(key: StreamableKey.Tmdb): Flow<Result<List<UnloadedVideoStreamRef>>> {
        return hostedTvDataRepository
            .getStreamableInfoByTmdbKey(hostedToTmdbMappingRepository, key)
            .flatMapLatest {
                it.mapNotNull { res -> res.getOrNull() }
                    .run { infosToFlowOfStreams(this) }
                    .map { streams -> Result.success(streams) }
            }
    }

    private fun infosToFlowOfStreams(infos: List<StreamableInfo.Hosted>): Flow<List<UnloadedVideoStreamRef>> {
        return infos
            .map {
                getStreamsByKey(it.key)
                    .onStart { emit(Result.success(emptyList())) }
                    .mapNotNull { result -> result.getOrNull() }
            }
            .combine()
            .map { sortByExtractionSupport(it.flatten()) }
    }

    private fun sortByExtractionSupport(videosWithLanguages: List<UnloadedVideoStreamRef>) =
        videosWithLanguages.sortedBy { vidWithLang -> !vidWithLang.linkExtractionSupported }
}