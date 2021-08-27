package com.tajmoti.libprimewiretvprovider

import com.tajmoti.libtvprovider.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
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

    override suspend fun search(query: String): Result<List<SearchResult>> {
        val pageSource = httpLoader(queryToSearchUrl(query))
            .getOrElse { return Result.failure(it) }
        return withContext(Dispatchers.Default) {
            parseSearchResultPageBlocking(pageSource)
        }
    }

    private fun queryToSearchUrl(query: String): String {
        val encoded = URLEncoder.encode(query, "utf-8")
        return "$baseUrl?s=$encoded&t=y&m=m&w=q"
    }

    override suspend fun getTvShow(key: String): Result<TvShowInfo> {
        val pageSource = httpLoader(baseUrl + key)
            .getOrElse { return Result.failure(it) }
        return withContext(Dispatchers.Default) {
            val document = Jsoup.parse(pageSource)
            val seasons = parseSearchResultPageBlockingSeason(key, document)
                .getOrElse { return@withContext Result.failure(it) }
            val tvItemInfo = parseTvItemInfo(key, document)
            val info = TvShowInfo(key, tvItemInfo, seasons)
            Result.success(info)
        }
    }

    private fun parseTvItemInfo(key: String, page: Document): TvItemInfo {
        val title =
            page.selectFirst(".stage_navigation > h1:nth-child(1) > span:nth-child(1) > a:nth-child(1)")!!
                .attr("title")
        val yearStart = title.indexOfLast { it == '(' }
        val yearEnd = title.indexOfLast { it == ')' }
        val yearStr = title.subSequence(yearStart + 1, yearEnd)
        val name = title.substring(0, yearStart - 1)
        return TvItemInfo(key, name, "en", yearStr.toString().toInt())
    }

    override suspend fun getMovie(movieKey: String): Result<MovieInfo> {
        TODO("Not yet implemented")
    }

    override suspend fun getStreamableLinks(episodeOrMovieKey: String): Result<List<VideoStreamRef>> {
        val html = loadHtmlFromUrl(baseUrl + episodeOrMovieKey)
            .getOrElse { return Result.failure(it) }
        return withContext(Dispatchers.Default) {
            getVideoStreamsBlocking(html, baseUrl)
        }
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