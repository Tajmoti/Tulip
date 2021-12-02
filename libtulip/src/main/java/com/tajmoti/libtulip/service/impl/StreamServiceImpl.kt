package com.tajmoti.libtulip.service.impl

import com.tajmoti.libtulip.model.info.LanguageCode
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
            .flatMapLatest { result -> result.fold(this::combineInfoWithLanguages) { flowOf(Result.failure(it)) } }
    }

    private fun combineInfoWithLanguages(result: StreamableInfo.Hosted): Flow<Result<List<UnloadedVideoStreamRef>>> {
        return hostedTvDataRepository.fetchStreams(result.key)
            .map { it.toResult().map { videoStreamRefs -> processRefs(videoStreamRefs, result) } }
    }

    private fun processRefs(refs: List<VideoStreamRef>, info: StreamableInfo.Hosted): List<UnloadedVideoStreamRef> {
        val unloadedVideos = refs.map { streamRef -> videoRefToUnloadedVideo(streamRef, info.language) }
        return sortByExtractionSupport(unloadedVideos)
    }

    private fun videoRefToUnloadedVideo(ref: VideoStreamRef, languageCode: LanguageCode): UnloadedVideoStreamRef {
        return UnloadedVideoStreamRef(ref, streamsRepo.canExtractStream(ref), languageCode)
    }

    override fun getStreamsByKey(key: StreamableKey.Tmdb): Flow<Result<List<UnloadedVideoStreamRef>>> {
        return hostedTvDataRepository.getStreamableInfoByTmdbKey(hostedToTmdbMappingRepository, key)
            .flatMapLatest { magic(it).map { streams -> Result.success(streams) } }
    }

    private fun magic(infoResultList: List<Result<StreamableInfo.Hosted>>): Flow<List<UnloadedVideoStreamRef>> {
        return infoResultList
            .map { infoResultToResultingInfoFlow(it) }
            .merge()
            .runningFold<List<UnloadedVideoStreamRef>, List<List<UnloadedVideoStreamRef>>>(emptyList()) { a, b ->
                a.plusElement(b)
            }
            .map { videos -> infoWithVideosToStreamsResult(videos) }
    }

    private fun infoWithVideosToStreamsResult(videos: List<List<UnloadedVideoStreamRef>>): List<UnloadedVideoStreamRef> {
        return sortByExtractionSupport(videos.flatten())
    }

    private fun infoResultToResultingInfoFlow(it: Result<StreamableInfo.Hosted>): Flow<List<UnloadedVideoStreamRef>> {
        return it.getOrNull()?.let { info -> getStreamsByKey(info.key).mapNotNull { it.getOrNull() } } ?: emptyFlow()
    }

    private fun sortByExtractionSupport(videosWithLanguages: List<UnloadedVideoStreamRef>) =
        videosWithLanguages.sortedBy { vidWithLang -> !vidWithLang.linkExtractionSupported }
}