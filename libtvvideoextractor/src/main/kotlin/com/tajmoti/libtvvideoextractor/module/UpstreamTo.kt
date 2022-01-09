package com.tajmoti.libtvvideoextractor.module

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.tajmoti.commonutils.substringBetween
import com.tajmoti.libtvvideoextractor.ExtractionError
import com.tajmoti.libtvvideoextractor.ExtractorModule
import com.tajmoti.libtvvideoextractor.RawPageSourceLoader
import com.tajmoti.libtvvideoextractor.WebDriverPageSourceLoader

class UpstreamTo : ExtractorModule {
    override val supportedUrls = listOf("upstream.to")
    override val supportedServiceNames = listOf("upstream.to")

    override suspend fun extractVideoUrl(
        url: String,
        rawLoader: RawPageSourceLoader,
        webDriverLoader: WebDriverPageSourceLoader
    ): Either<ExtractionError, String> {
        return webDriverLoader(url) { true }.fold(
            { sourceToVideoUrl(it).right() },
            { ExtractionError.Exception(it).left() }
        )
    }

    private fun sourceToVideoUrl(raw: String): String {
        val start = "video_ad|doPlay|prevt|captions|srt|file|"
        val end = "|remove|this|scrolling|frameborder|"
        val parts = raw.substringBetween(start, end).split("|")

        val start2 = "|m3u8|master|"
        val end2 = "|hls|sources|setup'.split('|')))"
        val parts2 = raw.substringBetween(start2, end2)

        return "https://${parts[2]}.${parts[1]}.${parts[0]}/hls/$parts2/master.m3u8"
    }
}