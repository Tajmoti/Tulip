package com.tajmoti.libtvvideoextractor.module

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.tajmoti.libtvvideoextractor.ExtractionError
import com.tajmoti.libtvvideoextractor.ExtractorModule
import com.tajmoti.libtvvideoextractor.RawPageSourceLoader
import com.tajmoti.libtvvideoextractor.WebDriverPageSourceLoader

class VoeSx : ExtractorModule {
    override val supportedUrls = listOf("voe.sx")
    override val supportedServiceNames = listOf("Voe.SX", "voe.sx")

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

    private fun sourceToVideoUrl(source: String): String {
        val startSequence = "\"hls\": \""
        val endSequence = "\""
        return source.substringBetween(startSequence, endSequence)
    }
}