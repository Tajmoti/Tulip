package com.tajmoti.libtulip.service.impl

import arrow.core.left
import arrow.core.right
import com.tajmoti.commonutils.*
import com.tajmoti.libtulip.model.MissingEntityException
import com.tajmoti.libtulip.model.info.*
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.StreamableKey
import com.tajmoti.libtulip.model.key.tvShowKey
import com.tajmoti.libtulip.model.stream.StreamableInfoWithLangLinks
import com.tajmoti.libtulip.model.stream.StreamableInfoWithLanguage
import com.tajmoti.libtulip.model.stream.UnloadedVideoStreamRef
import com.tajmoti.libtulip.model.stream.UnloadedVideoWithLanguage
import com.tajmoti.libtulip.repository.HostedTvDataRepository
import com.tajmoti.libtulip.repository.StreamsRepository
import com.tajmoti.libtulip.repository.TmdbTvDataRepository
import com.tajmoti.libtulip.service.LanguageMappingStreamService
import com.tajmoti.libtulip.service.StreamsResult
import com.tajmoti.libtvprovider.VideoStreamRef
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalCoroutinesApi::class)
class LanguageMappingStreamServiceImpl(
    private val hostedTvDataRepository: HostedTvDataRepository,
    private val streamsRepo: StreamsRepository,
    private val tvDataRepo: TmdbTvDataRepository
) : LanguageMappingStreamService {

    override suspend fun getStreamsWithLanguages(
        key: StreamableKey.Hosted
    ): StreamsResult {
        return hostedTvDataRepository.getStreamableInfo(key)
            .onFailure { logger.warn("Streamable info retrieval failed", it) }
            .flatMapZip { hostedTvDataRepository.fetchStreams(key) }
            .map { (infoWithLang, streams) ->
                val streamRefs = streams.map { rawVideoToUnloadedVideo(it) }
                combineInfoWithStreams(infoWithLang, streamRefs)
            }
            .fold({ it.right() }, { null.left() })
    }

    private fun rawVideoToUnloadedVideo(it: VideoStreamRef): UnloadedVideoStreamRef {
        return UnloadedVideoStreamRef(it, streamsRepo.canExtractFromService(it))
    }

    private fun combineInfoWithStreams(
        infoWithLang: StreamableInfoWithLanguage,
        streams: List<UnloadedVideoStreamRef>
    ) = streams
        .map { UnloadedVideoWithLanguage(it, infoWithLang.language) }
        .sortedBy { !it.video.linkExtractionSupported }
        .let { videos -> StreamableInfoWithLangLinks(infoWithLang.streamableInfo, videos) }

    override suspend fun getStreamsWithLanguages(
        key: StreamableKey.Tmdb
    ): Flow<StreamsResult> = flow {
        getStreamableInfo(key)
            .onSuccess { emit(StreamableInfoWithLangLinks(it, emptyList()).right()) }
            .onFailure { logger.warn("Failed to fetch streams for $key", it) }
            .onFailure { emit(null.left()) }
            .onSuccess { info ->
                getStreamablesWithLanguages(key)
                    .onSuccess { emitAll(combineInfoWithStreamables(it, info).map { it.right() }) }
                    .onFailure { logger.warn("Failed to retrieve streamables for $key", it) }
                    .onFailure { emit(info.left()) }
            }
    }

    private suspend fun getStreamableInfo(key: StreamableKey.Tmdb): Result<StreamableInfo> {
        return when (key) {
            is EpisodeKey.Tmdb -> getEpisodeInfo(key)
            is MovieKey.Tmdb -> getMovieInfo(key)
        }
    }

    private suspend fun getMovieInfo(key: MovieKey.Tmdb): Result<TulipMovie> {
        val name = tvDataRepo.getMovie(key)
            ?: return Result.failure(MissingEntityException)
        return Result.success(name)
    }

    private suspend fun getEpisodeInfo(key: EpisodeKey.Tmdb): Result<TulipCompleteEpisodeInfo> {
        val result = tvDataRepo.getFullEpisodeData(key)
            ?: return Result.failure(MissingEntityException)
        val (sh, ss, ep) = result
        val episode = TulipCompleteEpisodeInfo.Tmdb(
            key,
            sh.name,
            ep
        )
        return Result.success(episode)
    }


    private suspend fun combineInfoWithStreamables(
        listOfPairs: List<Pair<StreamableInfo.Hosted, LanguageCode>>,
        info: StreamableInfo
    ): Flow<StreamableInfoWithLangLinks> {
        return getStreamsAndMapLanguages(listOfPairs)
            .map { videosWithLanguages ->
                val videos = videosWithLanguages
                    .sortedBy { vidWithLang -> !vidWithLang.video.linkExtractionSupported }
                StreamableInfoWithLangLinks(info, videos)
            }
    }

    private suspend fun getStreamablesWithLanguages(
        info: StreamableKey.Tmdb
    ): Result<List<Pair<StreamableInfo.Hosted, LanguageCode>>> {
        return when (info) {
            is MovieKey.Tmdb -> getMovieStreamsWithLanguages(info)
            is EpisodeKey.Tmdb -> getEpisodeStreamsWithLanguages(info)
        }.onFailure { logger.warn("Streamable language info retrieval failed for $info", it) }
    }

    private suspend fun getEpisodeStreamsWithLanguages(info: EpisodeKey.Tmdb) =
        hostedTvDataRepository.getCompleteEpisodesByTmdbId(info)
            .map { it.parallelMap { ep -> getLanguagesForEpisode(ep.info).pairWithReverse(ep) } }
            .map { it.mapNotNull { langResult -> langResult.getOrNull() } }

    private suspend fun getMovieStreamsWithLanguages(info: MovieKey.Tmdb) =
        hostedTvDataRepository.getMovieByTmdbId(info)
            .map { it.map { movie -> movie to movie.language } }

    private suspend fun getLanguagesForEpisode(ep: TulipEpisodeInfo.Hosted) =
        hostedTvDataRepository.getTvShow(ep.key.tvShowKey)
            .onFailure { logger.warn("TV Show info retrieval failed for $ep", it) }
            .map { LanguageCode(it.info.language) }

    private suspend fun getStreamsAndMapLanguages(
        streamables: List<Pair<StreamableInfo.Hosted, LanguageCode>>
    ): Flow<List<UnloadedVideoWithLanguage>> {
        return streamables.parallelMapToFlow { (s, l) -> combineStreamableWithLanguage(s.key, l) }
            .filterNotNull()
            .runningFold(emptyList<Pair<StreamableInfoWithLangLinks, LanguageCode>>()) { a, b ->
                a + b
            }
            .map {
                it.flatMapWithTransform(
                    { pair -> pair.first.streams },
                    { pair, lang -> lang.video to pair.second }
                )
                    .map { (video, lang) -> UnloadedVideoWithLanguage(video, lang) }
            }
    }

    private suspend fun combineStreamableWithLanguage(
        streamable: StreamableKey.Hosted,
        language: LanguageCode
    ) = getStreamsWithLanguages(streamable)
        .orNull()
        ?.let { videos -> videos to language }
}