package com.tajmoti.libprimewiretvprovider

import com.tajmoti.commonutils.logger
import com.tajmoti.libtvprovider.VideoStreamRef
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

abstract class PrimewireEpisodeOrMovie(
    private val baseUrl: String,
    internal val episodeUrl: String,
    private val pageLoader: SimplePageSourceLoader
) {
    val key = episodeUrl

    suspend fun loadSources(): Result<List<VideoStreamRef>> {
        val html = pageLoader.invoke(baseUrl + episodeUrl)
            .getOrElse { return Result.failure(it) }
        return withContext(Dispatchers.Default) {
            getVideoStreams(html)
        }
    }

    private suspend fun getVideoStreams(html: String): Result<List<VideoStreamRef>> {
        return try {
            val coroutines = coroutineScope {
                Jsoup.parse(html)
                    .getElementsByClass("movie_version")
                    .filterNot(this@PrimewireEpisodeOrMovie::isAd)
                    .map { async(Dispatchers.Default) { itemToStreamRefBlocking(it) } }
            }
            Result.success(awaitAll(*coroutines.toTypedArray()))
        } catch (e: Throwable) {
            logger.warn("Request failed", e)
            Result.failure(e)
        }
    }

    private suspend fun itemToStreamRefBlocking(element: Element): VideoStreamRef {
        val link = element
            .getElementsByClass("movie_version_link")
            .first()!!
            .getElementsByClass("propper-link")
            .attr("href")
        val name = element
            .getElementsByClass("version-host")
            .text()
        val redirectUrl = baseUrl + link
        val hostingUrl = getVideoHostingUrlBlocking(redirectUrl)
        return VideoStreamRef(name, hostingUrl ?: redirectUrl, hostingUrl != null)
    }

    private suspend fun getVideoHostingUrlBlocking(redirectUrl: String): String? {
        return try {
            withContext(Dispatchers.IO) {
                Jsoup.connect(redirectUrl).get().baseUri()
            }
        } catch (e: Throwable) {
            logger.warn("Request failed", e)
            return null
        }
    }

    private fun isAd(element: Element): Boolean {
        return element.hasClass("nopop")
    }
}
