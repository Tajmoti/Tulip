package com.tajmoti.libtulip.service.impl

import com.tajmoti.commonutils.mapToAsyncJobs
import com.tajmoti.libtulip.model.MissingEntityException
import com.tajmoti.libtulip.model.hosted.HostedEpisode
import com.tajmoti.libtulip.model.hosted.HostedStreamable
import com.tajmoti.libtulip.model.info.LanguageCode
import com.tajmoti.libtulip.model.info.StreamableInfo
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.StreamableKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.model.stream.StreamableInfoWithLangLinks
import com.tajmoti.libtulip.model.stream.UnloadedVideoWithLanguage
import com.tajmoti.libtulip.service.HostedTvDataService
import com.tajmoti.libtulip.service.LanguageMappingStreamService
import com.tajmoti.libtulip.service.StreamExtractorService
import com.tajmoti.libtulip.service.TvDataService
import javax.inject.Inject

class LanguageMappingStreamServiceImpl @Inject constructor(
    private val hostedTvDataService: HostedTvDataService,
    private val extractorService: StreamExtractorService,
    private val tvDataService: TvDataService
) : LanguageMappingStreamService {

    override suspend fun getStreamsWithLanguagesByHostedKey(
        key: StreamableKey.Hosted,
        infoConsumer: (StreamableInfo) -> Unit
    ): Result<StreamableInfoWithLangLinks> {
        val newInfo = hostedTvDataService.getStreamableInfo(key)
            .getOrElse { return Result.failure(it) }
        infoConsumer(newInfo.streamableInfo)
        val result = extractorService.fetchStreams(key.streamingService, key.streamableKey)
            .getOrElse { return Result.failure(it) }
            .map { UnloadedVideoWithLanguage(it, LanguageCode(newInfo.language)) }
            .sortedBy { !it.video.linkExtractionSupported }
        return Result.success(StreamableInfoWithLangLinks(newInfo.streamableInfo, result))
    }

    override suspend fun getStreamsByTmdbKey(
        key: StreamableKey.Tmdb,
        infoConsumer: (StreamableInfo) -> Unit
    ): Result<StreamableInfoWithLangLinks> {
        val streamableInfo = tvDataService.getStreamableInfo(key)
            .getOrElse { return Result.failure(it) }
        infoConsumer(streamableInfo)
        val x = getStreamablesWithLanguages(key)
            .getOrElse { return Result.failure(it) }
        val streams = getStreamsAndMapLanguages(x)
            .sortedBy { !it.video.linkExtractionSupported }
        return Result.success(StreamableInfoWithLangLinks(streamableInfo, streams))
    }

    private suspend fun getStreamablesWithLanguages(
        info: StreamableKey.Tmdb
    ): Result<List<Pair<HostedStreamable, LanguageCode>>> {
        val result = when (info) {
            is MovieKey.Tmdb -> {
                val movie = hostedTvDataService.getMovieByTmdbId(info)
                movie.map { it.map { movie -> movie to LanguageCode(movie.language) } }
                    .getOrElse { return Result.failure(it) }
            }
            is EpisodeKey.Tmdb -> {
                hostedTvDataService.getEpisodeByTmdbId(info)
                    .map { mapLanguagesParallel(it) }
                    .getOrElse { return Result.failure(it) }
                    .mapNotNull { it.getOrNull() }
            }
        }
        return Result.success(result)
    }

    private suspend fun mapLanguagesParallel(
        it: List<HostedEpisode>
    ): List<Result<Pair<HostedEpisode, LanguageCode>>> {
        return mapToAsyncJobs(it) { ep ->
            val hostedKey = TvShowKey.Hosted(ep.service, ep.tvShowKey)
            val show = hostedTvDataService.getTvShow(hostedKey)
                .getOrElse { return@mapToAsyncJobs Result.failure(MissingEntityException) }
            Result.success(ep to LanguageCode(show.info.language))
        }
    }

    private suspend fun getStreamsAndMapLanguages(
        streamables: List<Pair<HostedStreamable, LanguageCode>>
    ): List<UnloadedVideoWithLanguage> {
        return mapToAsyncJobs(streamables) {
            getStreamsWithLanguagesByHostedKey(it.first.hostedKey) {}
                .getOrNull()
                ?.let { videos -> videos to it.second }
        }
            .filterNotNull()
            .flatMap { it.first.streams.map { video -> video to it.second } }
            .map { UnloadedVideoWithLanguage(it.first.video, it.second) }
    }
}