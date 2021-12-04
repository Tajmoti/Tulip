package com.tajmoti.libopensubtitles

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Streaming

interface OpenSubtitlesFallbackService {

    @GET("en/subtitleserve/sub/{file_id}")
    @Streaming
    suspend fun downloadSubtitlesFallback(
        @Path("file_id") fileId: Long
    ): ResponseBody
}