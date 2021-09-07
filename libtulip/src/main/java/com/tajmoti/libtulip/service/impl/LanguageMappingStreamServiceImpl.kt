package com.tajmoti.libtulip.service.impl

import com.tajmoti.commonutils.*
import com.tajmoti.libtulip.model.hosted.HostedEpisode
import com.tajmoti.libtulip.model.hosted.HostedStreamable
import com.tajmoti.libtulip.model.info.LanguageCode
import com.tajmoti.libtulip.model.info.StreamableInfo
import com.tajmoti.libtulip.model.info.StreamableInfoWithLanguage
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.StreamableKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.model.stream.StreamableInfoWithLangLinks
import com.tajmoti.libtulip.model.stream.UnloadedVideoStreamRef
import com.tajmoti.libtulip.model.stream.UnloadedVideoWithLanguage
import com.tajmoti.libtulip.service.HostedTvDataService
import com.tajmoti.libtulip.service.LanguageMappingStreamService
import com.tajmoti.libtulip.service.StreamExtractorService
import com.tajmoti.libtulip.service.TvDataService
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class LanguageMappingStreamServiceImpl @Inject constructor(
    private val hostedTvDataService: HostedTvDataService,
    private val extractorService: StreamExtractorService,
    private val tvDataService: TvDataService
) : LanguageMappingStreamService {

    override suspend fun getStreamsWithLanguages(
        key: StreamableKey.Hosted,
        infoConsumer: (StreamableInfo) -> Unit
    ): Result<StreamableInfoWithLangLinks> {
        return hostedTvDataService.getStreamableInfo(key)
            .onSuccess { infoConsumer(it.streamableInfo) to it }
            .onFailure { logger.warn("Streamable info retrieval failed", it) }
            .flatMapZip { extractorService.fetchStreams(key.streamingService, key.streamableKey) }
            .map { (infoWithLang, streams) -> combineInfoWithStreams(infoWithLang, streams) }
    }

    private fun combineInfoWithStreams(
        infoWithLang: StreamableInfoWithLanguage,
        streams: List<UnloadedVideoStreamRef>
    ) = streams
        .map { UnloadedVideoWithLanguage(it, LanguageCode(infoWithLang.language)) }
        .sortedBy { !it.video.linkExtractionSupported }
        .let { videos -> StreamableInfoWithLangLinks(infoWithLang.streamableInfo, videos) }

    override suspend fun getStreamsWithLanguages(
        key: StreamableKey.Tmdb,
        infoConsumer: (StreamableInfo) -> Unit
    ): Result<Flow<StreamableInfoWithLangLinks>> {
        return tvDataService.getStreamableInfo(key)
            .onFailure { logger.warn("Failed to fetch streams for $key", it) }
            .onSuccess(infoConsumer)
            .flatMapZip { getStreamablesWithLanguages(key) }
            .onFailure { logger.warn("Failed to retrieve streamables for $key", it) }
            .map { (info, listOfPairs) -> combineInfoWithStreamables(listOfPairs, info) }
    }

    private suspend fun combineInfoWithStreamables(
        listOfPairs: List<Pair<HostedStreamable, LanguageCode>>,
        info: StreamableInfo
    ) = getStreamsAndMapLanguages(listOfPairs)
        .map {
            val vid = it.sortedBy { videoWithLang -> !videoWithLang.video.linkExtractionSupported }
            StreamableInfoWithLangLinks(info, vid)
        }

    private suspend fun getStreamablesWithLanguages(
        info: StreamableKey.Tmdb
    ): Result<List<Pair<HostedStreamable, LanguageCode>>> {
        return when (info) {
            is MovieKey.Tmdb -> getMovieStreamsWithLanguages(info)
            is EpisodeKey.Tmdb -> getEpisodeStreamsWithLanguages(info)
        }.onFailure { logger.warn("Streamable language info retrieval failed for $info", it) }
    }

    private suspend fun getEpisodeStreamsWithLanguages(info: EpisodeKey.Tmdb) =
        hostedTvDataService.getEpisodeByTmdbId(info)
            .map { it.parallelMap { ep -> getLanguagesForEpisode(ep).pairWithReverse(ep) } }
            .map { it.mapNotNull { langResult -> langResult.getOrNull() } }

    private suspend fun getMovieStreamsWithLanguages(info: MovieKey.Tmdb) =
        hostedTvDataService.getMovieByTmdbId(info)
            .map { it.map { movie -> movie to LanguageCode(movie.language) } }

    private suspend fun getLanguagesForEpisode(ep: HostedEpisode) =
        TvShowKey.Hosted(ep.service, ep.tvShowKey)
            .let { hostedTvDataService.getTvShow(it) }
            .onFailure { logger.warn("TV Show info retrieval failed for $ep", it) }
            .map { LanguageCode(it.info.language) }

    private suspend fun getStreamsAndMapLanguages(
        streamables: List<Pair<HostedStreamable, LanguageCode>>
    ): Flow<List<UnloadedVideoWithLanguage>> {
        return streamables.parallelMapToFlow { (s, l) -> combineStreamableWithLanguage(s, l) }
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
        streamable: HostedStreamable,
        language: LanguageCode
    ) = getStreamsWithLanguages(streamable.hostedKey) {}
        .getOrNull()
        ?.let { videos -> videos to language }
}