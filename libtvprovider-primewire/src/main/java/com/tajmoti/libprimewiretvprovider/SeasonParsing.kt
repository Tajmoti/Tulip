package com.tajmoti.libprimewiretvprovider

import com.tajmoti.libtvprovider.EpisodeInfo
import com.tajmoti.libtvprovider.Season
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element


internal fun parseSearchResultPageBlockingSeason(
    tvShowKey: String,
    page: Document
): Result<List<Season>> {
    return try {
        val items = page
            .getElementsByClass("show_season")
            .map { elemToSeason(tvShowKey, it) }
        Result.success(items)
    } catch (e: Throwable) {
        Result.failure(e)
    }
}

private fun elemToSeason(tvShowKey: String, element: Element): Season {
    val number = element
        .previousElementSibling()!!
        .getElementsByTag("a")
        .first()!!
        .ownText()
        .replaceFirst("► Season ", "")
        .toInt()
    val episodes = element
        .getElementsByClass("tv_episode_item")
        .map { elemToEpisode(it) }
    return Season(tvShowKey, number, episodes)
}

private fun elemToEpisode(element: Element): EpisodeInfo {
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
    return EpisodeInfo(url, number ?: 0, name, null) // TODO Overview
}