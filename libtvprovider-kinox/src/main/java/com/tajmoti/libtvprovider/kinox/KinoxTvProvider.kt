package com.tajmoti.libtvprovider.kinox

import com.tajmoti.libtvprovider.TvItem
import com.tajmoti.libtvprovider.TvProvider
import com.tajmoti.libtvprovider.show.Episode
import com.tajmoti.libtvprovider.show.Season
import com.tajmoti.libtvprovider.stream.Streamable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.net.URLEncoder

class KinoxTvProvider(
    private val baseUrl: String = "https://kinox.to"
) : TvProvider {

    override suspend fun search(query: String): Result<List<TvItem>> {
        return withContext(Dispatchers.IO) {
            return@withContext searchBlocking(query)
        }
    }

    override suspend fun getShow(key: String, info: TvItem.Show.Info): Result<TvItem.Show> {
        val show = KinoxShow(info.name, baseUrl, key)
        return Result.success(show)
    }

    override suspend fun getSeason(key: String, info: Season.Info): Result<Season> {
        val episodes = info.episodeInfo.map(this::serializedEpToEp)
        val show = KinoxSeason(info.number, episodes)
        return Result.success(show)
    }

    override suspend fun getStreamable(key: String, info: Streamable.Info): Result<Streamable> {
        val name = info.name
        val url = key
        val result = KinoxEpisode(name, baseUrl, url)
        // TODO This can also be a movie
        return Result.success(result)
    }

    private fun serializedEpToEp(it: Episode.Info): KinoxEpisode {
        return KinoxEpisode(it.name, baseUrl, it.key)
    }

    private fun searchBlocking(query: String): Result<List<TvItem>> {
        return try {
            val items = Jsoup.connect(queryToSearchUrl(query))
                .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                .get()
                .select("#RsltTableStatic > tbody:nth-child(2)")
                .first()!!
                .children()
                .mapNotNull(this::elemToTvItem)
            Result.success(items)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    private fun queryToSearchUrl(query: String): String {
        val encoded = URLEncoder.encode(query, "utf-8")
        return "$baseUrl/Search.html?q=$encoded"
    }

    private fun elemToTvItem(element: Element): TvItem? {
        // val langIcon = element.child(0).child(0).attr("src") TODO multiple languages
        val type = element.child(1).child(0).attr("title")
        val titleElem = element.child(2).child(0)
        val title = titleElem.text()
        val link = titleElem.attr("href")
        return when (type) {
            "series" -> KinoxShow(title, baseUrl, link)
            "movie" -> null // TODO movies
            else -> null
        }
    }
}