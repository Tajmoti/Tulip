package com.tajmoti.libtvprovider.kinox

import com.tajmoti.commonutils.flatMapWithContext
import com.tajmoti.libtvprovider.*
import kotlinx.coroutines.Dispatchers
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URLEncoder

class KinoxTvProvider(
    private val httpLoader: SimplePageSourceLoader,
    private val baseUrl: String = "https://kinox.to",
    /**
     * Whether search results which don't have a first air year set (it's set to 0)
     * should be thrown away. This is an optimization to throw away trash results.
     */
    private val throwAwayItemsWithNoYear: Boolean = true
) : TvProvider {

    override suspend fun search(query: String): Result<List<SearchResult>> {
        return httpLoader(queryToSearchUrl(query))
            .flatMapWithContext(Dispatchers.Default) {
                parseSearchResultPageBlocking(it, throwAwayItemsWithNoYear)
            }
    }

    private fun queryToSearchUrl(query: String): String {
        val encoded = URLEncoder.encode(query, "utf-8")
        return "$baseUrl/Search.html?q=$encoded"
    }

    override suspend fun getTvShow(id: String): Result<TvShowInfo> {
        return httpLoader(baseUrl + id)
            .flatMapWithContext(Dispatchers.Default) {
                val document = Jsoup.parse(it)
                parseSeasonsBlocking(id, document)
                    .map { seasons -> TvShowInfo(parseTvShowInfo(id, document), seasons) }
            }
    }

    private fun parseTvShowInfo(key: String, document: Document): TvItemInfo {
        val name =
            document.selectFirst("div.leftOpt:nth-child(3) > h1:nth-child(1) > span:nth-child(1)")!!
                .ownText()
        val yearStr = document.selectFirst(".Year")!!
            .ownText()
        val year = yearStr.replace("(", "").replace(")", "")
        val flag = document.select(".Flag > img:nth-child(1)").attr("src")
            .removeSuffix(".png")
            .replaceBeforeLast("/", "")
            .substring(1)
            .toInt()
        return TvItemInfo(key, name, languageNumberToLanguageCode(flag), year.toInt())
    }


    override suspend fun getMovie(id: String): Result<MovieInfo> {
        TODO()
    }

    override suspend fun getStreamableLinks(episodeOrMovieId: String): Result<List<VideoStreamRef>> {
        return httpLoader(baseUrl + episodeOrMovieId)
            .flatMapWithContext(Dispatchers.Default) {
                fetchSources(baseUrl, it, httpLoader)
            }
    }
}