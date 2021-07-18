package com.tajmoti.libprimewiretvprovider

import com.tajmoti.libtvprovider.TvItem
import com.tajmoti.libtvprovider.show.Season
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

data class PrimewireShow(
    override val name: String,
    private val baseUrl: String,
    private val showUrl: String,
    private val pageLoader: suspend (url: String) -> String
) : TvItem.Show {
    override val key = PrimewireTvShowId(name, showUrl)

    override suspend fun fetchSeasons(): Result<List<Season>> {
        return withContext(Dispatchers.IO) {
            return@withContext searchBlocking()
        }
    }

    private fun searchBlocking(): Result<List<Season>> {
        return try {
            val items = Jsoup.connect(baseUrl + showUrl)
                .get()
                .getElementsByClass("show_season")
                .map(this::elemToSeason)
            Result.success(items)
        } catch (e: Throwable) {
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

    private fun elemToEpisode(element: Element): PrimewireEpisodeOrMovie {
        val name = element
            .getElementsByClass("tv_episode_name")
            .text()
            .replaceFirst("- ", "")
        val url = element
            .getElementsByTag("a")
            .first()!!
            .attr("href")
        return PrimewireEpisodeOrMovie(name, baseUrl, url, pageLoader)
    }
}
