package com.tajmoti.libtvvideoextractor.module

import com.tajmoti.commonutils.flatMap
import com.tajmoti.libtvvideoextractor.ExtractorModule
import com.tajmoti.libtvvideoextractor.RawPageSourceLoader
import com.tajmoti.libtvvideoextractor.WebDriverPageSourceLoader

class StreamzzTo : ExtractorModule {
    override val supportedUrls = listOf("streamzz.to")
    override val supportedServiceNames = listOf("streamzz.to", "streamz.cc")


    override suspend fun extractVideoUrl(
        url: String,
        rawLoader: RawPageSourceLoader,
        webDriverLoader: WebDriverPageSourceLoader
    ): Result<String> {
        return rawLoader(url)
            .flatMap { parseResults(it) }
    }

    private fun parseResults(source: String): Result<String> {
        return runCatching {
            val token = VIDEO_URL_REGEX.find(source)!!.groupValues[1]
            "https://get.streamz.tw/getlink-$token.dll"
        }
    }

    companion object {
        private val VIDEO_URL_REGEX = "video3\\|src\\|(.*)\\|type\\|video_3".toRegex()
    }
}