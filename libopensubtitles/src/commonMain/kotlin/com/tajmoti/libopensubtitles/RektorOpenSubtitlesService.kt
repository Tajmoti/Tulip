package com.tajmoti.libopensubtitles

import com.tajmoti.libopensubtitles.model.download.DownloadSubtitlesRequestBody
import com.tajmoti.libopensubtitles.model.search.SubtitlesResponse
import com.tajmoti.rektor.Rektor
import com.tajmoti.rektor.Template
import com.tajmoti.rektor.params
import io.ktor.client.statement.*
import io.ktor.utils.io.*

class RektorOpenSubtitlesService(private val rektor: Rektor) : OpenSubtitlesService {
    private val searchEpisode = Template.get<SubtitlesResponse>("/api/v1/subtitles")
    private val searchMovie = Template.get<SubtitlesResponse>("/api/v1/subtitles")
    private val downloadSubtitles = Template.get<HttpResponse>("/api/v1/download")


    override suspend fun searchEpisode(tmdbTvId: Long, seasonNumber: Int, episodeNumber: Int): SubtitlesResponse {
        return rektor.execute(
            searchEpisode,
            queryParams = params(
                "parent_tmdb_id" to tmdbTvId,
                "season_number" to seasonNumber,
                "episode_number" to episodeNumber,
            )
        )
    }

    override suspend fun searchMovie(tmdbMovieId: Long): SubtitlesResponse {
        return rektor.execute(
            searchMovie,
            queryParams = params(
                "tmdb_id" to tmdbMovieId
            )
        )
    }

    override suspend fun downloadSubtitles(
        requestBody: DownloadSubtitlesRequestBody,
        contentType: String
    ): ByteReadChannel {
        val response = rektor.execute(
            downloadSubtitles,
            headers = mapOf("Content-Type" to contentType),
            requestBody = requestBody
        )
        return response.bodyAsChannel()
    }
}