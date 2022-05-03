package com.tajmoti.libtulip.repository.impl

import com.tajmoti.commonutils.logger
import com.tajmoti.libopensubtitles.OpenSubtitlesFallbackService
import com.tajmoti.libopensubtitles.OpenSubtitlesService
import com.tajmoti.libopensubtitles.model.search.SubtitlesResponse
import com.tajmoti.libtulip.model.key.*
import com.tajmoti.libtulip.model.subtitle.SubtitleInfo
import com.tajmoti.libtulip.repository.SubtitleRepository

class SubtitleRepositoryImpl(
    private val openSubtitlesService: OpenSubtitlesService,
    private val openSubtitlesFallbackService: OpenSubtitlesFallbackService,
) : SubtitleRepository {

    override suspend fun getAvailableSubtitles(key: StreamableKey): Result<List<SubtitleInfo>> {
        return fetchForTmdbId(key)
            .map { it.data.mapNotNull { subtitlesResponse -> subtitlesResponse.fromApi() } }
            .onFailure { logger.warn { "Subtitle list fetch failed for $key" } }
    }

    private suspend fun fetchForTmdbId(itemId: StreamableKey): Result<SubtitlesResponse> {
        return when (itemId) {
            is EpisodeKey.Tmdb -> runCatching {
                openSubtitlesService.searchEpisode(itemId.tvShowKey.id, itemId.seasonNumber, itemId.episodeNumber)
            }
            is MovieKey.Tmdb -> runCatching {
                openSubtitlesService.searchMovie(itemId.id)
            }
            is EpisodeKey.Hosted -> {
                Result.failure(NotImplementedError()) // TODO
            }
            is MovieKey.Hosted -> {
                Result.failure(NotImplementedError()) // TODO
            }
        }
    }
}