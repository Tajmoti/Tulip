package com.tajmoti.libtvvideoextractor

interface ExtractorModule {
    val supportedUrl: String


    suspend fun extractVideoUrl(url: String, pageLoader: PageSourceLoader): Result<String>
}