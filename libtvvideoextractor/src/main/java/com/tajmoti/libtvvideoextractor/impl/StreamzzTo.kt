package com.tajmoti.libtvvideoextractor.impl

import com.tajmoti.libtvvideoextractor.ExtractorModule
import com.tajmoti.libtvvideoextractor.PageSourceLoader
import org.jsoup.Jsoup

class StreamzzTo : ExtractorModule {
    override val supportedUrl = "streamzz.to"


    override suspend fun extractVideoUrl(url: String, loader: PageSourceLoader): Result<String> {
        return try {
            val source = loader(url, 2, this::checkUrl)
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
        return (url.contains(supportedUrl) || url.contains("cdn"))
                && !url.endsWith(".png") && !url.endsWith(".jpg")
    }
}