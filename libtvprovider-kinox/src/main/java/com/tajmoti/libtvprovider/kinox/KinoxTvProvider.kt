package com.tajmoti.libtvprovider.kinox

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

class KinoxTvProvider(
    private val httpLoader: SimplePageSourceLoader,
    private val baseUrl: String = "https://kinox.to",
) : TvProvider {

    override suspend fun search(query: String): Result<List<TvItem>> {
        val pageSource = httpLoader(queryToSearchUrl(query))
            .getOrElse { return Result.failure(it) }
        return withContext(Dispatchers.Default) {
            parseSearchResultPageBlocking(pageSource)
        }
    }

    override suspend fun getShow(info: TvItem.Show.Info): Result<TvItem.Show> {
        val show =
            KinoxShow(info.name, info.name, baseUrl, info.key, info.firstAirDateYear, httpLoader)
        return Result.success(show)
    }

    override suspend fun getSeason(info: Season.Info): Result<Season> {
        val episodes = info.episodes.map(this::serializedEpToEp).sorted()
        val show = KinoxSeason(info.number, episodes)
        return Result.success(show)
    }

    override suspend fun getEpisode(info: Episode.Info): Result<Episode> {
        return Result.success(serializedEpToEp(info))
    }

    override suspend fun getMovie(info: TvItem.Movie.Info): Result<TvItem.Movie> {
        TODO()
    }

    private fun serializedEpToEp(it: Episode.Info): KinoxEpisode {
        return KinoxEpisode(it.number, it.name, baseUrl, it.key, httpLoader)
    }

    private fun parseSearchResultPageBlocking(pageSource: String): Result<List<TvItem>> {
        return try {
            val items = Jsoup.parse(pageSource)
                .select("#RsltTableStatic > tbody:nth-child(2)")
                .first()!!
                .children()
                .mapNotNull(this::elemToTvItem)
            Result.success(items)
        } catch (e: Throwable) {
            logger.warn("Request failed", e)
            Result.failure(e)
        }
    }

    private fun queryToSearchUrl(query: String): String {
        val encoded = URLEncoder.encode(query, "utf-8")
        return "$baseUrl/Search.html?q=$encoded"
    }

    private fun elemToTvItem(element: Element): TvItem? {
        val langIcon = element.child(0).child(0).attr("src")
        val languageNumber = langIcon
            .removeSuffix(".png")
            .replaceBeforeLast("/", "")
            .substring(1)
            .toInt()
        val language = languageNumberToLanguageCode(languageNumber)
        val type = element.child(1).child(0).attr("title")
        val titleElem = element.child(2).child(0)
        val title = titleElem.text()
        val link = titleElem.attr("href")
        val firstAirYear = element.getElementsByClass("Year")
            .first()!!
            .ownText()
            .toInt()
        return when (type) {
            "series" -> KinoxShow(title, language, baseUrl, link, firstAirYear, httpLoader)
            "movie" -> null // TODO movies
            else -> null
        }
    }

    private fun languageNumberToLanguageCode(number: Int): String {
        return when (number) {
            1 -> "de"
            else -> "en"
        }
    }
}