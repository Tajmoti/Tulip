package com.tajmoti.libtulip.repository.impl

import arrow.core.left
import arrow.core.right
import com.tajmoti.commonutils.flatMap
import com.tajmoti.libtulip.model.MissingEntityException
import com.tajmoti.libtulip.model.info.LanguageCode
import com.tajmoti.libtulip.model.info.StreamableInfo
import com.tajmoti.libtulip.model.info.TulipCompleteEpisodeInfo
import com.tajmoti.libtulip.model.info.seasonNumber
import com.tajmoti.libtulip.model.key.*
import com.tajmoti.libtulip.model.stream.StreamableInfoWithLinks
import com.tajmoti.libtulip.model.stream.UnloadedVideoStreamRef
import com.tajmoti.libtulip.repository.HostedTvDataRepository
import com.tajmoti.libtulip.repository.StreamRepository
import com.tajmoti.libtulip.repository.StreamsResult
import com.tajmoti.libtulip.repository.TmdbTvDataRepository
import com.tajmoti.libtulip.service.StreamExtractionService
import com.tajmoti.libtvprovider.VideoStreamRef
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class StreamRepositoryImpl(
    private val hostedTvDataRepository: HostedTvDataRepository,
    private val streamsRepo: StreamExtractionService,
    private val tvDataRepo: TmdbTvDataRepository
) : StreamRepository {

    override fun getStreamsByKey(key: StreamableKey.Hosted): Flow<StreamsResult> {
        return hostedTvDataRepository.getStreamableInfo(key)
            .flatMapLatest { result -> result.fold(this::combineInfoWithLanguages) { flowOf(Result.failure(it)) } }
            .map { result -> result.fold({ it.right() }, { null.left() }) }
    }

    private fun combineInfoWithLanguages(result: StreamableInfo.Hosted): Flow<Result<StreamableInfoWithLinks>> {
        return hostedTvDataRepository.fetchStreams(result.key)
            .map { it.toResult().map { videoStreamRefs -> processRefs(videoStreamRefs, result) } }
    }

    private fun processRefs(refs: List<VideoStreamRef>, info: StreamableInfo.Hosted): StreamableInfoWithLinks {
        val unloadedVideos = refs.map { streamRef -> videoRefToUnloadedVideo(streamRef, info.language) }
        return StreamableInfoWithLinks(info, sortByExtractionSupport(unloadedVideos))
    }

    private fun videoRefToUnloadedVideo(ref: VideoStreamRef, languageCode: LanguageCode): UnloadedVideoStreamRef {
        return UnloadedVideoStreamRef(ref, streamsRepo.canExtractStream(ref), languageCode)
    }

    override fun getStreamsByKey(
        key: StreamableKey.Tmdb
    ): Flow<StreamsResult> {
        val infoFlow = getStreamableInfo(key)
        val streamableFlow = hostedTvDataRepository.getStreamableInfoByTmdbKey(key)
            // Makes sure that a value from [getStreamableInfo] can be consumed immediately
            // if it is produced earlier than the first value of [getStreamablesWithLanguages]
            .onStart { emit(emptyList()) }
        return combine(infoFlow, streamableFlow) { info, infoResultList ->
            val infoValue = info.getOrNull()
            if (infoResultList.isNotEmpty() && infoValue != null) {
                magic(infoResultList, infoValue)
            } else if (infoValue != null) {
                flowOf<StreamsResult>(infoValue.left())
            } else {
                flowOf<StreamsResult>(null.left())
            }
        }.flatMapLatest { it }
    }

    private fun magic(
        infoResultList: List<Result<StreamableInfo.Hosted>>,
        infoValue: StreamableInfo.Tmdb
    ): Flow<StreamsResult> {
        return infoResultList
            .map { infoResultToResultingInfoFlow(it) }
            .merge()
            .runningFold<StreamableInfoWithLinks, List<StreamableInfoWithLinks>>(emptyList()) { a, b -> a + b }
            .map { videos -> infoWithVideosToStreamsResult(infoValue, videos) }
    }

    private fun infoWithVideosToStreamsResult(
        infoValue: StreamableInfo.Tmdb,
        videos: List<StreamableInfoWithLinks>
    ): StreamsResult {
        val sorted = sortByExtractionSupport(videos.flatMap { it.streams })
        return StreamableInfoWithLinks(infoValue, sorted).right()
    }

    private fun infoResultToResultingInfoFlow(it: Result<StreamableInfo.Hosted>): Flow<StreamableInfoWithLinks> {
        return it.getOrNull()?.let { info -> getStreamsByKey(info.key).mapNotNull { it.orNull() } } ?: emptyFlow()
    }

    private fun sortByExtractionSupport(videosWithLanguages: List<UnloadedVideoStreamRef>) =
        videosWithLanguages.sortedBy { vidWithLang -> !vidWithLang.linkExtractionSupported }

    private fun getFullEpisodeData(key: EpisodeKey.Tmdb): Flow<Result<TulipCompleteEpisodeInfo.Tmdb>> {
        return tvDataRepo.getTvShow(key.tvShowKey)
            .map {
                it.toResult().flatMap { tvInfo ->
                    val seasons = tvInfo.seasons
                        .firstOrNull { season -> key.seasonNumber == season.seasonNumber }
                        ?: return@map Result.failure(MissingEntityException)
                    val episode = seasons.episodes
                        .firstOrNull { episode -> episode.episodeNumber == key.episodeNumber }
                        ?: return@map Result.failure(MissingEntityException)
                    Result.success(TulipCompleteEpisodeInfo.Tmdb(key, tvInfo.name, episode))
                }
            }
    }

    private fun getStreamableInfo(key: StreamableKey.Tmdb): Flow<Result<StreamableInfo.Tmdb>> {
        return when (key) {
            is EpisodeKey.Tmdb -> getFullEpisodeData(key)
            is MovieKey.Tmdb -> tvDataRepo.getMovie(key).map { it.toResult() }
        }
    }
}