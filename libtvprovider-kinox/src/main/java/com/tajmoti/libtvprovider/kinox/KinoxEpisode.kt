package com.tajmoti.libtvprovider.kinox

import com.tajmoti.commonutils.logger
import com.tajmoti.libtvprovider.Episode
import com.tajmoti.libtvprovider.VideoStreamRef
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

data class KinoxEpisode(
    override val number: Int?,
    override val name: String?,
    private val baseUrl: String,
    internal val episodeUrl: String,
    private val httpLoader: SimplePageSourceLoader
) : Episode {
    override val key = episodeUrl

    override suspend fun loadSources(): Result<List<VideoStreamRef>> {
        val page = httpLoader(baseUrl + episodeUrl)
            .getOrElse { return Result.failure(it) }
        return withContext(Dispatchers.Default) {
            fetchSources(page)
        }
    }

    private suspend fun fetchSources(pageSource: String): Result<List<VideoStreamRef>> {
        return try {
            val links = Jsoup.parse(pageSource)
                .getElementsByTag("ul")
                .first()!!
                .children()
                .mapNotNull { elementToStream(it) }
            Result.success(links)
        } catch (e: Exception) {
            logger.warn("Request failed", e)
            Result.failure(e)
        }
    }

    private suspend fun elementToStream(li: Element): VideoStreamRef? {
        val hoster = li.attr("rel")
        val url = "$baseUrl/aGET/Mirror/$hoster"
        val pageSource = httpLoader(url)
            .getOrElse { return null }
        val json = Jsoup.parse(pageSource)
            .body()
            .html()
            .replace("&quot;", "")
            .replace("\\", "")
        // Forgive me, father.
        val redirectUrl = REGEX_ANCHOR_URL.find(json)
            ?.groupValues
            ?.get(1) ?: return null
        val name = REGEX_NAME.find(json)
            ?.groupValues
            ?.get(1)
        val realUrl = withContext(Dispatchers.IO) {
            Jsoup.connect(redirectUrl).get().baseUri()
        }
        return VideoStreamRef(name ?: "Unknown", realUrl, true)
    }

    companion object {
        val REGEX_ANCHOR_URL = Regex("<a href=\"(.*)\" target=\"_blank\">")
        val REGEX_NAME = Regex("\"HosterName\":\"(.*?)\"")
    }
}
