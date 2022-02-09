package com.tajmoti.libopensubtitles

import io.ktor.utils.io.*

interface OpenSubtitlesFallbackService {

//    @GET("en/subtitleserve/sub/{file_id}")
//    @Streaming
    suspend fun downloadSubtitlesFallback(
        /* @Path("file_id") */ fileId: Long
    ): ByteReadChannel
}