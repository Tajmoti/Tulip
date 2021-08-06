package com.tajmoti.libtvprovider.kinox

import com.tajmoti.libtvprovider.show.Episode
import com.tajmoti.libtvprovider.stream.VideoStreamRef
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

data class KinoxEpisode(
    override val number: Int?,
    override val name: String?,
    private val baseUrl: String,
    internal val episodeUrl: String
) : Episode {
    override val key = episodeUrl

    override suspend fun loadSources(): Result<List<VideoStreamRef>> {
        return withContext(Dispatchers.IO) {
            return@withContext fetchSourcesBlocking()
        }
    }

    private fun fetchSourcesBlocking(): Result<List<VideoStreamRef>> {
        return try {
            val links = Jsoup.connect(baseUrl + episodeUrl)
                .get()
                .getElementsByTag("ul")
                .first()!!
                .children()
                .mapNotNull(this::elementToStream)
            Result.success(links)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun elementToStream(li: Element): VideoStreamRef? {
        val hoster = li.attr("rel")
        val url = "$baseUrl/aGET/Mirror/$hoster"
        val json = Jsoup.connect(url)
            .get()
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
        val realUrl = Jsoup.connect(redirectUrl).get().baseUri()
        return VideoStreamRef(name ?: "Unknown", realUrl, true)
    }

    companion object {
        val REGEX_ANCHOR_URL = Regex("<a href=\"(.*)\" target=\"_blank\">")
        val REGEX_NAME = Regex("\"HosterName\":\"(.*?)\"")
    }
}
