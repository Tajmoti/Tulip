package com.tajmoti.libprimewiretvprovider

import com.tajmoti.libtvprovider.TvItem
import com.tajmoti.libtvprovider.TvProvider
import com.tajmoti.libtvprovider.show.Season
import com.tajmoti.libtvprovider.stream.Streamable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.Serializable
import java.net.URLEncoder

class PrimewireTvProvider(
    /**
     * Loads the page with the provided URL including
     * running JS on it, and returns the resulting HTML.
     *
     * The urlBlocker function is a predicate, which can be used to
     * stop useless requests (e.g. scripts and images).
     * Return true to allow the request, false to block it.
     *
     * This allows encoded and obfuscated stream URLs to be accessed.
     */
    private val pageLoader: suspend (url: String, urlBlocker: (String) -> Boolean) -> String,
    /**
     * Base URL of the primewire domain, in case it changes.
     */
    private val baseUrl: String = "https://www.primewire.li"
) : TvProvider {

    override suspend fun search(query: String): Result<List<TvItem>> {
        return withContext(Dispatchers.IO) {
            return@withContext searchBlocking(query)
        }
    }

    override suspend fun getShow(key: Serializable): Result<TvItem.Show> {
        if (key !is PrimewireTvShowId)
            return Result.failure(ClassCastException("Primewire show ID must be of type PrimewireItemId"))
        val show = PrimewireShow(key.name, baseUrl, key.url, this::loadHtmlFromUrl)
        return Result.success(show)
    }

    override suspend fun getSeason(key: Serializable): Result<Season> {
        if (key !is PrimewireSeasonId)
            return Result.failure(ClassCastException("Primewire season ID must be of type PrimewireItemId"))
        val episodes = key.episodes.map(this::serializedEpToEp)
        val show = PrimewireSeason(key.number, episodes)
        return Result.success(show)
    }

    override suspend fun getStreamable(key: Serializable): Result<Streamable> {
        if (key !is PrimewireStreamableId)
            return Result.failure(ClassCastException("Primewire streamable ID must be of type PrimewireStreamableId"))
        val name = key.name
        val url = key.streamPageUrl
        val result = PrimewireEpisodeOrMovie(name, baseUrl, url, this::loadHtmlFromUrl)
        // TODO This can also be a movie
        return Result.success(result)
    }

    private fun serializedEpToEp(it: PrimewireSeasonId.EpisodeInfo): PrimewireEpisodeOrMovie {
        return PrimewireEpisodeOrMovie(it.name, baseUrl, it.url, this::loadHtmlFromUrl)
    }

    private fun searchBlocking(query: String): Result<List<TvItem>> {
        return try {
            val items = Jsoup.connect(queryToSearchUrl(query))
                .get()
                .getElementsByClass("index_item")
                .map(this::elemToTvItem)
            Result.success(items)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    private fun elemToTvItem(element: Element): TvItem {
        val anchor = element.getElementsByTag("a")
            .firstOrNull()!!
        val name = anchor
            .attr("title")
        val itemUrl = anchor
            .attr("href")
        val isShow = itemUrl
            .startsWith("/tv/")
        return if (isShow) {
            PrimewireShow(name, baseUrl, itemUrl, this::loadHtmlFromUrl)
        } else {
            PrimewireEpisodeOrMovie(name, baseUrl, itemUrl, this::loadHtmlFromUrl)
        }
    }

    private fun queryToSearchUrl(query: String): String {
        val encoded = URLEncoder.encode(query, "utf-8")
        return "$baseUrl?s=$encoded&t=y&m=m&w=q"
    }

    private suspend fun loadHtmlFromUrl(url: String): String {
        return pageLoader.invoke(url, this::shouldAllowUrl)
    }

    private fun shouldAllowUrl(url: String): Boolean {
        return url.contains(baseUrl) && URL_BLACKLIST.none { url.contains(it) }
    }

    companion object {
        private val URL_BLACKLIST = listOf("/comments/", "/css/", "/spiderman")
    }
}