package com.tajmoti.libtvvideoextractor.module

import com.tajmoti.libtvvideoextractor.ExtractorModule
import com.tajmoti.libtvvideoextractor.PageSourceLoader
import org.jsoup.Jsoup

class StreamzzTo : ExtractorModule {
    override val supportedUrls = listOf("streamzz.to")


    override suspend fun extractVideoUrl(url: String, loader: PageSourceLoader): Result<String> {
        val source = loader(url, 2, this::checkUrl)
            .getOrElse { return Result.failure(it) }
        return try {
            val document = Jsoup.parse(source)
            val src = document.getElementsByTag("video")
                .first()!!
                .attr("src")
            Result.success(src)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    private fun checkUrl(url: String): Boolean {
        return (url.contains(supportedUrls[0]) || url.contains("cdn"))
                && !url.endsWith(".png") && !url.endsWith(".jpg")
    }
}