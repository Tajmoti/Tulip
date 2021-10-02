package com.tajmoti.libtvprovider.kinox

import com.tajmoti.libtvprovider.EpisodeInfo
import com.tajmoti.libtvprovider.Season
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

internal fun parseSeasonsBlocking(tvShowKey: String, page: Document): Result<List<Season>> {
    return try {
        val dropdown = page
            .select("html body div#frmMain div#dontbeevil div#Vadda div.MirrorModule div select#SeasonSelection")
            .first() ?: return Result.failure(SeasonsNotPresentException)
        val rel = dropdown.attr("rel")
        val result = dropdown.children()
            .map { optionToSeason(tvShowKey, it, rel) }
        Result.success(result)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

private fun optionToSeason(tvShowKey: String, option: Element, rel: String): Season {
    val number = option.attr("value").toInt()
    val episodes = option.attr("rel")
        .split(',')
        .map { it.toInt() }
        .map { episodeNumToEpisode(it, number, rel) }
    return Season(tvShowKey, number, episodes)
}

private fun episodeNumToEpisode(
    episode: Int,
    season: Int,
    rel: String
): EpisodeInfo {
    val episodeUrl = "/aGET/MirrorByEpisode/$rel&Season=$season&Episode=$episode"
    return EpisodeInfo(episodeUrl, episode, "Kinox Episode", null) // TODO Overview
}