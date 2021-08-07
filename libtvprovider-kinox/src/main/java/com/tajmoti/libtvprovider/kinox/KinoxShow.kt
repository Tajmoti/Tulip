package com.tajmoti.libtvprovider.kinox

import com.tajmoti.libtvprovider.TvItem
import com.tajmoti.libtvprovider.Season
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

data class KinoxShow(
    override val name: String,
    override val language: String,
    private val baseUrl: String,
    private val showUrl: String
) : TvItem.Show {
    override val key = showUrl

    override suspend fun fetchSeasons(): Result<List<Season>> {
        return withContext(Dispatchers.IO) {
            return@withContext fetchSeasonsBlocking()
        }
    }

    private fun fetchSeasonsBlocking(): Result<List<Season>> {
        return try {
            val dropdown = Jsoup.connect(baseUrl + showUrl)
                .get()
                .select("html body div#frmMain div#dontbeevil div#Vadda div.MirrorModule div select#SeasonSelection")
                .first()!!
            val rel = dropdown.attr("rel")
            val result = dropdown.children()
                .map { optionToSeason(it, rel) }
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun optionToSeason(option: Element, rel: String): Season {
        val number = option.attr("value").toInt()
        val episodes = option.attr("rel")
            .split(',')
            .map { it.toInt() }
            .map { episodeNumToEpisode(it, number, rel) }
        return KinoxSeason(number, episodes)
    }

    private fun episodeNumToEpisode(episode: Int, season: Int, rel: String): KinoxEpisode {
        val episodeUrl = "/aGET/MirrorByEpisode/$rel&Season=$season&Episode=$episode"
        return KinoxEpisode(episode, null, baseUrl, episodeUrl)
    }
}
