package com.tajmoti.libprimewiretvprovider

import com.tajmoti.commonutils.logger
import com.tajmoti.libtvprovider.Season
import com.tajmoti.libtvprovider.TvItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

data class PrimewireShow(
    override val name: String,
    private val baseUrl: String,
    private val showUrl: String,
    override val firstAirDateYear: Int?,
    private val pageLoader: SimplePageSourceLoader,
    private val httpLoader: SimplePageSourceLoader
) : TvItem.Show {
    override val language = "en"
    override val key = showUrl

    override suspend fun fetchSeasons(): Result<List<Season>> {
        val pageSource = httpLoader(baseUrl + showUrl)
            .getOrElse { return Result.failure(it) }
        return withContext(Dispatchers.Default) {
            parseSearchResultPageBlocking(pageSource)
        }
    }

    private fun parseSearchResultPageBlocking(pageSource: String): Result<List<Season>> {
        return try {
            val items = Jsoup.parse(pageSource)
                .getElementsByClass("show_season")
                .map(this::elemToSeason)
            Result.success(items)
        } catch (e: Throwable) {
            logger.warn("Request failed", e)
            Result.failure(e)
        }
    }

    private fun elemToSeason(element: Element): Season {
        val number = element
            .previousElementSibling()!!
            .getElementsByTag("a")
            .first()!!
            .ownText()
            .replaceFirst("► Season ", "")
            .toInt()
        val episodes = element
            .getElementsByClass("tv_episode_item")
            .map(this::elemToEpisode)

        return PrimewireSeason(number, episodes)
    }

    private fun elemToEpisode(element: Element): PrimewireEpisode {
        val elemText = element.text()
        val number = if (elemText.startsWith("Special")) {
            null
        } else {
            elemText.replaceAfter(" ", "")
                .substringBefore(" ")
                .replaceFirst("E", "")
                .toInt()
        }

        val name = element
            .getElementsByClass("tv_episode_name")
            .text()
            .replaceFirst("- ", "")
        val url = element
            .getElementsByTag("a")
            .first()!!
            .attr("href")
        return PrimewireEpisode(number, name, baseUrl, url, pageLoader)
    }
}
