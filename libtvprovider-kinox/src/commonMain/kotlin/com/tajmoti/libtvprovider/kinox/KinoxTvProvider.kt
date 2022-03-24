package com.tajmoti.libtvprovider.kinox

import com.tajmoti.commonutils.PageSourceLoader
import com.tajmoti.commonutils.UrlEncoder
import com.tajmoti.commonutils.flatMap
import com.tajmoti.ksoup.KSoup
import com.tajmoti.libtvprovider.TvProvider
import com.tajmoti.libtvprovider.model.SearchResult
import com.tajmoti.libtvprovider.model.TvItem
import com.tajmoti.libtvprovider.model.VideoStreamRef

class KinoxTvProvider(
    private val loader: PageSourceLoader,
    private val baseUrl: String = "https://kinoz.to",
    /**
     * Whether search results which don't have a first air year set (it's set to 0)
     * should be thrown away. This is an optimization to throw away trash results.
     */
    private val throwAwayItemsWithNoYear: Boolean = true
) : TvProvider {

    override suspend fun search(query: String): Result<List<SearchResult>> {
        return loader.loadWithBrowser(queryToSearchUrl(query))
            .flatMap { parseSearchResultPageBlocking(it, throwAwayItemsWithNoYear) }
    }

    private fun queryToSearchUrl(query: String): String {
        val encoded = UrlEncoder.encode(query)
        return "$baseUrl/Search.html?q=$encoded"
    }

    override suspend fun getTvShow(id: String): Result<TvItem.TvShow> {
        return loader.loadWithBrowser(baseUrl + id)
            .flatMap {
                val document = KSoup.parse(it)
                parseSeasonsBlocking(document)
                    .map { seasons -> TvItem.TvShow(parseTvItemInfo(id, document), seasons) }
            }
    }

    override suspend fun getMovie(id: String): Result<TvItem.Movie> {
        return loader.loadWithBrowser(baseUrl + id)
            .map {
                val document = KSoup.parse(it)
                TvItem.Movie(parseTvItemInfo(id, document))
            }
    }

    override suspend fun getStreamableLinks(episodeOrMovieId: String): Result<List<VideoStreamRef>> {
        return loader.loadWithBrowser(baseUrl + episodeOrMovieId)
            .flatMap { fetchSources(baseUrl, it, loader) }
    }
}