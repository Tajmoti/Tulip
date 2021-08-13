package com.tajmoti.libprimewiretvprovider

import com.tajmoti.commonutils.logger
import com.tajmoti.libtvprovider.Episode
import com.tajmoti.libtvprovider.Season
import com.tajmoti.libtvprovider.TvItem
import com.tajmoti.libtvprovider.TvProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.net.URLEncoder

class PrimewireTvProvider(
    /**
     * Loads web pages into a real browser to run any JS loading obfuscated data.
     */
    private val pageLoader: PageSourceLoader,
    /**
     * Only performs a GET request, does not run any JS.
     */
    private val httpLoader: SimplePageSourceLoader,
    /**
     * Base URL of the primewire domain, in case it changes.
     */
    private val baseUrl: String = "https://www.primewire.li"
) : TvProvider {

    override suspend fun search(query: String): Result<List<TvItem>> {
        val pageSource = httpLoader(queryToSearchUrl(query))
            .getOrElse { return Result.failure(it) }
        return withContext(Dispatchers.Default) {
            parseSearchResultPageBlocking(pageSource)
        }
    }

    override suspend fun getShow(info: TvItem.Show.Info): Result<TvItem.Show> {
        val show = PrimewireShow(info.name, baseUrl, info.key, this::loadHtmlFromUrl, httpLoader)
        return Result.success(show)
    }

    override suspend fun getSeason(info: Season.Info): Result<Season> {
        val primewireEpisodes = info.episodes.map(this::serializedEpToEp).sorted()
        val show = PrimewireSeason(info.number, primewireEpisodes)
        return Result.success(show)
    }

    override suspend fun getEpisode(info: Episode.Info): Result<Episode> {
        return Result.success(serializedEpToEp(info))
    }

    override suspend fun getMovie(info: TvItem.Movie.Info): Result<TvItem.Movie> {
        val result = PrimewireMovie(info.name, baseUrl, info.key, this::loadHtmlFromUrl)
        return Result.success(result)
    }

    private fun serializedEpToEp(it: Episode.Info): PrimewireEpisode {
        return PrimewireEpisode(it.number, it.name, baseUrl, it.key, this::loadHtmlFromUrl)
    }

    private fun parseSearchResultPageBlocking(pageSource: String): Result<List<TvItem>> {
        return try {
            val items = Jsoup.parse(pageSource)
                .getElementsByClass("index_item")
                .map(this::elemToTvItem)
            Result.success(items)
        } catch (e: Throwable) {
            logger.warn("Request failed", e)
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
            PrimewireShow(name, baseUrl, itemUrl, this::loadHtmlFromUrl, httpLoader)
        } else {
            PrimewireMovie(name, baseUrl, itemUrl, this::loadHtmlFromUrl)
        }
    }

    private fun queryToSearchUrl(query: String): String {
        val encoded = URLEncoder.encode(query, "utf-8")
        return "$baseUrl?s=$encoded&t=y&m=m&w=q"
    }

    private suspend fun loadHtmlFromUrl(url: String): Result<String> {
        return pageLoader.invoke(url, this::shouldAllowUrl)
    }

    private fun shouldAllowUrl(url: String): Boolean {
        return url.contains(baseUrl) && URL_BLACKLIST.none { url.contains(it) }
    }

    companion object {
        private val URL_BLACKLIST = listOf("/comments/", "/css/", "/spiderman")
    }
}