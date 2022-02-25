package com.tajmoti.libtvvideoextractor.module

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.tajmoti.commonutils.substringBetween
import com.tajmoti.libtvvideoextractor.ExtractionError
import com.tajmoti.libtvvideoextractor.ExtractorModule
import com.tajmoti.commonutils.PageSourceLoader

class VoeSx : ExtractorModule {
    override val supportedUrls = listOf("voe.sx")
    override val supportedServiceNames = listOf("Voe.SX", "voe.sx")

    override suspend fun extractVideoUrl(url: String, loader: PageSourceLoader): Either<ExtractionError, String> {
        return loader.loadWithGet(url).fold(
            { sourceToVideoUrl(it).right() },
            { ExtractionError.Exception(it).left() }
        )
    }

    private fun sourceToVideoUrl(source: String): String {
        val startSequence = "\"hls\": \""
        val endSequence = "\""
        return source.substringBetween(startSequence, endSequence)
    }

    override suspend fun doBeforePlayback(url: String, loader: PageSourceLoader) {

    }
}