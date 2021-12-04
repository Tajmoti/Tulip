@file:Suppress("UNUSED")

package com.tajmoti.libopensubtitles

import com.tajmoti.libopensubtitles.model.download.DownloadSubtitlesRequestBody
import com.tajmoti.libopensubtitles.model.search.SubtitlesResponse
import okhttp3.ResponseBody
import retrofit2.http.*

interface OpenSubtitlesService {

    @GET("/api/v1/subtitles")
    suspend fun searchEpisode(
        @Query("parent_tmdb_id") tmdbTvId: Long,
        @Query("season_number") seasonNumber: Int,
        @Query("episode_number") episodeNumber: Int
    ): SubtitlesResponse

    @GET("/api/v1/subtitles")
    suspend fun searchMovie(
        @Query("tmdb_id") tmdbMovieId: Long
    ): SubtitlesResponse

    @POST("/api/v1/download")
    @Streaming
    suspend fun downloadSubtitles(
        @Body requestBody: DownloadSubtitlesRequestBody,
        @Header("Content-Type") contentType: String = "multipart/form-data"
    ): ResponseBody
}