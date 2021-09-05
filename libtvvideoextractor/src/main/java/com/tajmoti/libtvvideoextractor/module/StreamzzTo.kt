package com.tajmoti.libtvvideoextractor.module

import com.tajmoti.commonutils.flatMap
import com.tajmoti.commonutils.logger
import com.tajmoti.libtvvideoextractor.ExtractorModule
import com.tajmoti.libtvvideoextractor.PageSourceLoaderWithLoadCount
import org.jsoup.Jsoup

class StreamzzTo : ExtractorModule {
    override val supportedUrls = listOf("streamzz.to")
    override val supportedServiceNames = listOf("streamzz.to", "streamz.cc")


    override suspend fun extractVideoUrl(
        url: String,
        loader: PageSourceLoaderWithLoadCount
    ): Result<String> {
        return loader(url, 2, this::checkUrl)
            .flatMap { parseResults(it) }
    }

    private fun parseResults(source: String): Result<String> {
        return runCatching {
            Jsoup.parse(source)
                .getElementsByTag("video")
                .first()!!
                .attr("src")
        }.onFailure { logger.warn("Request failed", it) }
    }

    private fun checkUrl(url: String): Boolean {
        return (url.contains(supportedUrls[0]) || url.contains("cdn"))
                && !url.endsWith(".png") && !url.endsWith(".jpg")
    }
}