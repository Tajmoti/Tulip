package com.tajmoti.libprimewiretvprovider

import com.tajmoti.commonutils.logger
import com.tajmoti.libtvprovider.VideoStreamRef
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

abstract class PrimewireEpisodeOrMovie(
    private val baseUrl: String,
    private val episodeUrl: String,
    private val pageLoader: SimplePageSourceLoader
) {
    val key = episodeUrl

    suspend fun loadSources(): Result<List<VideoStreamRef>> {
        val html = pageLoader.invoke(baseUrl + episodeUrl)
            .getOrElse { return Result.failure(it) }
        return withContext(Dispatchers.Default) {
            getVideoStreamsBlocking(html)
        }
    }

    private fun getVideoStreamsBlocking(html: String): Result<List<VideoStreamRef>> {
        return try {
            val streams = Jsoup.parse(html)
                .getElementsByClass("movie_version")
                .filterNot(this@PrimewireEpisodeOrMovie::isAd)
                .map { itemToStreamRef(it) }
            Result.success(streams)
        } catch (e: Throwable) {
            logger.warn("Request failed", e)
            Result.failure(e)
        }
    }

    private fun itemToStreamRef(element: Element): VideoStreamRef {
        val link = element
            .getElementsByClass("movie_version_link")
            .first()!!
            .getElementsByClass("propper-link")
            .attr("href")
        val name = element
            .getElementsByClass("version-host")
            .text()
        val redirectUrl = baseUrl + link
        return VideoStreamRef.Unresolved(name, redirectUrl)
    }

    private fun isAd(element: Element): Boolean {
        return element.hasClass("nopop")
    }
}
