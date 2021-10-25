package com.tajmoti.libtulip.repository.impl

import com.tajmoti.commonutils.logger
import com.tajmoti.libopensubtitles.OpenSubtitlesFallbackService
import com.tajmoti.libopensubtitles.OpenSubtitlesService
import com.tajmoti.libopensubtitles.model.search.SubtitlesResponse
import com.tajmoti.libtulip.model.key.*
import com.tajmoti.libtulip.model.subtitle.SubtitleInfo
import com.tajmoti.libtulip.repository.SubtitleRepository
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class SubtitleRepositoryImpl(
    private val openSubtitlesService: OpenSubtitlesService,
    private val openSubtitlesFallbackService: OpenSubtitlesFallbackService,
) : SubtitleRepository {

    override suspend fun fetchAvailableSubtitles(itemId: StreamableKey): Result<List<SubtitleInfo>> {
        val subtitleItems = fetchForTmdbId(itemId)
            .onFailure { logger.warn("Subtitle list fetch failed for $itemId", it) }
            .getOrElse { return Result.failure(it) }
            .data.mapNotNull { it.fromApi() }
        return Result.success(subtitleItems)
    }

    private suspend fun fetchForTmdbId(
        itemId: StreamableKey
    ): Result<SubtitlesResponse> {
        return when (itemId) {
            is EpisodeKey.Tmdb -> runCatching {
                val tvId = itemId.tvShowKey.id.id
                val season = itemId.seasonNumber
                val episode = itemId.episodeNumber
                openSubtitlesService.searchEpisode(tvId, season, episode)
            }
            is MovieKey.Tmdb -> runCatching {
                openSubtitlesService.searchMovie(itemId.id.id)
            }
            is EpisodeKey.Hosted -> {
                Result.failure(NotImplementedError()) // TODO
            }
            is MovieKey.Hosted -> {
                Result.failure(NotImplementedError()) // TODO
            }
        }
    }

    override suspend fun downloadSubtitle(info: SubtitleInfo): Result<InputStream> {
//        val request = DownloadSubtitlesRequestBody(info.fileId)
//        return runCatching { openSubtitlesService.downloadSubtitles(request) }
//            .map { it.byteStream() }
        logger.debug("Downloading subtitles by $info")
        return runCatching { openSubtitlesFallbackService.downloadSubtitlesFallback(info.legacyId) }
            .map { it.byteStream() }
            .map { ZipInputStream(it) }
            .mapCatching {
                getSubtitleFileFromZip(it)
                    ?: throw NoSuchElementException("Subtitle file not present in the zip")
            }
    }

    private fun getSubtitleFileFromZip(zis: ZipInputStream): InputStream? {
        var current: ZipEntry?
        while (zis.nextEntry.also { current = it } != null) {
            if (current!!.name.endsWith(".srt"))
                return zis
        }
        return null
    }
}