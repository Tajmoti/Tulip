package com.tajmoti.libtvprovider.kinox

import com.tajmoti.commonutils.LibraryDispatchers
import com.tajmoti.commonutils.flatMap
import com.tajmoti.libtvprovider.TvProvider
import com.tajmoti.libtvprovider.model.SearchResult
import com.tajmoti.libtvprovider.model.TvItem
import com.tajmoti.libtvprovider.model.VideoStreamRef
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.net.URLEncoder
import kotlin.coroutines.CoroutineContext

class KinoxTvProvider(
    private val httpLoader: SimplePageSourceLoader,
    private val baseUrl: String = "https://kinoz.to",
    /**
     * Whether search results which don't have a first air year set (it's set to 0)
     * should be thrown away. This is an optimization to throw away trash results.
     */
    private val throwAwayItemsWithNoYear: Boolean = true,
    private val dispatcher: CoroutineContext = LibraryDispatchers.libraryContext,
) : TvProvider {

    override suspend fun search(query: String): Result<List<SearchResult>> {
        return withContext(dispatcher) {
            httpLoader(queryToSearchUrl(query))
                .flatMap { parseSearchResultPageBlocking(it, throwAwayItemsWithNoYear) }
        }
    }

    private fun queryToSearchUrl(query: String): String {
        val encoded = URLEncoder.encode(query, "utf-8")
        return "$baseUrl/Search.html?q=$encoded"
    }

    override suspend fun getTvShow(id: String): Result<TvItem.TvShow> {
        return withContext(dispatcher) {
            httpLoader(baseUrl + id)
                .flatMap {
                    val document = Jsoup.parse(it)
                    parseSeasonsBlocking(document)
                        .map { seasons -> TvItem.TvShow(parseTvItemInfo(id, document), seasons) }
                }
        }
    }

    override suspend fun getMovie(id: String): Result<TvItem.Movie> {
        return withContext(dispatcher) {
            httpLoader(baseUrl + id)
                .map {
                    val document = Jsoup.parse(it)
                    TvItem.Movie(parseTvItemInfo(id, document))
                }
        }
    }

    override suspend fun getStreamableLinks(episodeOrMovieId: String): Result<List<VideoStreamRef>> {
        return withContext(dispatcher) {
            httpLoader(baseUrl + episodeOrMovieId)
                .flatMap { fetchSources(baseUrl, it, httpLoader) }
        }
    }
}