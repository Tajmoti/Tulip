package com.tajmoti.libprimewiretvprovider

import com.tajmoti.libtvprovider.stream.Streamable
import com.tajmoti.libtvprovider.stream.VideoStreamRef
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

abstract class PrimewireEpisodeOrMovie(
    private val baseUrl: String,
    internal val episodeUrl: String,
    private val pageLoader: SimplePageSourceLoader
) : Streamable {
    override val key = episodeUrl

    override suspend fun loadSources(): Result<List<VideoStreamRef>> {
        val html = pageLoader.invoke(baseUrl + episodeUrl)
            .getOrElse { return Result.failure(it) }
        return withContext(Dispatchers.IO) {
            return@withContext getVideoStreams(html)
        }
    }

    private suspend fun getVideoStreams(html: String): Result<List<VideoStreamRef>> {
        return try {
            val coroutines = coroutineScope {
                Jsoup.parse(html)
                    .getElementsByClass("movie_version")
                    .filterNot(this@PrimewireEpisodeOrMovie::isAd)
                    .map { async { itemToStreamRef(it) } }
            }
            Result.success(awaitAll(*coroutines.toTypedArray()))
        } catch (e: Throwable) {
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
        val hostingUrl = getVideoHostingUrl(redirectUrl)
        return VideoStreamRef(name, hostingUrl ?: redirectUrl, hostingUrl != null)
    }

    private fun getVideoHostingUrl(redirectUrl: String): String? {
        return try {
            Jsoup.connect(redirectUrl).get().baseUri()
        } catch (e: Throwable) {
            return null
        }
    }

    private fun isAd(element: Element): Boolean {
        return element.hasClass("nopop")
    }
}
