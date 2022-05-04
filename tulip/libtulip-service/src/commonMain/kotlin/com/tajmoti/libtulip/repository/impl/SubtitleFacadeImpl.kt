package com.tajmoti.libtulip.repository.impl

import com.tajmoti.commonutils.logger
import com.tajmoti.libopensubtitles.OpenSubtitlesFallbackService
import com.tajmoti.libopensubtitles.OpenSubtitlesService
import com.tajmoti.libopensubtitles.model.search.SubtitleAttributes
import com.tajmoti.libopensubtitles.model.search.SubtitlesResponse
import com.tajmoti.libopensubtitles.model.search.SubtitlesResponseData
import com.tajmoti.libtulip.dto.SubtitleDto
import com.tajmoti.libtulip.facade.SubtitleFacade
import com.tajmoti.libtulip.model.key.*

class SubtitleFacadeImpl(
    private val openSubtitlesService: OpenSubtitlesService,
    @Suppress("unused")
    private val openSubtitlesFallbackService: OpenSubtitlesFallbackService,
) : SubtitleFacade {

    override suspend fun getAvailableSubtitles(key: StreamableKey): Result<List<SubtitleDto>> {
        return fetchForTmdbId(key)
            .map { it.data.mapNotNull { subtitlesResponse -> subtitlesResponse.fromApi() } }
            .onFailure { logger.warn(it) { "Subtitle list fetch failed for $key" } }
    }

    private fun SubtitlesResponseData.fromApi(): SubtitleDto? {
        return attributes.fromApi()
    }

    private fun SubtitleAttributes.fromApi(): SubtitleDto? {
        val file = data.firstOrNull()?.fileId ?: return null
        return SubtitleDto(SubtitleKey(subtitleId, legacySubtitleId), release, language, file)
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