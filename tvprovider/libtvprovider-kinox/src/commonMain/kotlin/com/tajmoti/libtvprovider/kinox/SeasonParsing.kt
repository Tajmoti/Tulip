package com.tajmoti.libtvprovider.kinox

import com.tajmoti.ksoup.KDocument
import com.tajmoti.ksoup.KElement
import com.tajmoti.libtvprovider.model.EpisodeInfo
import com.tajmoti.libtvprovider.model.Season

internal fun parseSeasonsBlocking(page: KDocument): Result<List<Season>> {
    return try {
        val dropdown = page
            .select("html body div#frmMain div#dontbeevil div#Vadda div.MirrorModule div select#SeasonSelection")
            .firstOrNull() ?: return Result.failure(SeasonsNotPresentException)
        val rel = dropdown.attr("rel")
        val result = dropdown.children()
            .map { optionToSeason(it, rel) }
        Result.success(result)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

private fun optionToSeason(option: KElement, rel: String): Season {
    val number = option.attr("value").toInt()
    val episodes = option.attr("rel")
        .split(',')
        .map { it.toInt() }
        .map { episodeNumToEpisode(it, number, rel) }
    return Season(number, episodes)
}

private fun episodeNumToEpisode(
    episode: Int,
    season: Int,
    rel: String
): EpisodeInfo {
    val episodeUrl = "/aGET/MirrorByEpisode/$rel&Season=$season&Episode=$episode"
    return EpisodeInfo(episodeUrl, episode, "Kinox Episode", null) // TODO Overview
}