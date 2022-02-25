package com.tajmoti.libtvvideoextractor.module

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.tajmoti.commonutils.PageSourceLoader
import com.tajmoti.libtvvideoextractor.*

class StreamzzTo : ExtractorModule {
    override val supportedUrls = listOf("streamzz.to")
    override val supportedServiceNames = listOf("streamzz.to", "streamz.cc")

    override suspend fun extractVideoUrl(url: String, loader: PageSourceLoader): Either<ExtractionError, String> {
        return loader.loadWithGet(url)
            .fold({ parseResults(it) }, { ExtractionError.Exception(it).left() })
    }

    override suspend fun doBeforePlayback(url: String, loader: PageSourceLoader) {
        // This is needed to trick per-IP scraping protection
        loader.loadWithGetLocal("https://streamzz.to/count.php?bcd=1")
    }

    private fun parseResults(source: String): Either<ExtractionError, String> {
        val token = VIDEO_URL_REGEX.find(source)?.groupValues?.get(1)
        return when {
            token != null ->
                "https://get.streamz.tw/getlink-$token.dll".right()
            source.contains("Click here to verify yourself as human.") ->
                ExtractionError.Captcha(CAPTCHA_INFO).left()
            else ->
                ExtractionError.UnexpectedPageContent.left()
        }
    }

    companion object {
        private val VIDEO_URL_REGEX = "video3\\|src\\|(.*)\\|type\\|video_3".toRegex()
        private val CAPTCHA_INFO = CaptchaInfo(
                "https://streamzz.to/checkme.dll",
                "https://streamzz.to/docheckme.dll"
        )
    }
}