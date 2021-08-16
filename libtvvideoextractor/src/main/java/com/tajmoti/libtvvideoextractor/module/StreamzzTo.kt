package com.tajmoti.libtvvideoextractor.module

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
        val source = loader(url, 2, this::checkUrl)
            .getOrElse { return Result.failure(it) }
        return try {
            val document = Jsoup.parse(source)
            val src = document.getElementsByTag("video")
                .first()!!
                .attr("src")
            Result.success(src)
        } catch (e: Throwable) {
            logger.warn("Request failed", e)
            Result.failure(e)
        }
    }

    private fun checkUrl(url: String): Boolean {
        return (url.contains(supportedUrls[0]) || url.contains("cdn"))
                && !url.endsWith(".png") && !url.endsWith(".jpg")
    }
}