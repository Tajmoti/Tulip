package com.tajmoti.libtvvideoextractor.module

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.tajmoti.commonutils.substringBetween
import com.tajmoti.libtvvideoextractor.ExtractionError
import com.tajmoti.libtvvideoextractor.ExtractorModule
import com.tajmoti.libtvvideoextractor.RawPageSourceLoader
import com.tajmoti.libtvvideoextractor.WebDriverPageSourceLoader

class TheVideoMe : ExtractorModule {
    override val supportedUrls = listOf("thevideome.com")
    override val supportedServiceNames = listOf("TheVideo.me")

    override suspend fun extractVideoUrl(
        url: String,
        rawLoader: RawPageSourceLoader,
        webDriverLoader: WebDriverPageSourceLoader
    ): Either<ExtractionError, String> {
        return rawLoader(url).fold(
            { sourceToVideoUrl(it).right() },
            { ExtractionError.Exception(it).left() }
        )
    }

    private fun sourceToVideoUrl(raw: String): String {
        val start = "'video|mp4|src|var|videojs|type|"
        val end = "|video_1'.split('|')"
        return "https://thevideome.com/" + raw.substringBetween(start, end) + ".mp4"
    }
}