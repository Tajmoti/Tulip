package com.tajmoti.libprimewiretvprovider

import com.tajmoti.ksoup.KElement
import com.tajmoti.libtvprovider.model.EpisodeInfo
import com.tajmoti.libtvprovider.model.Season

internal fun parseSearchResultPageBlockingSeason(page: KElement): Result<List<Season>> {
    return try {
        val items = page
            .getElementsByClass("show_season")
            .map { elemToSeason(it) }
        Result.success(items)
    } catch (e: Throwable) {
        Result.failure(e)
    }
}

private fun elemToSeason(element: KElement): Season {
    val number = element
        .previousElementSibling()!!
        .getElementsByTag("a")
        .first()
        .ownText()
        .replaceFirst("► Season ", "")
        .toInt()
    val episodes = element
        .getElementsByClass("tv_episode_item")
        .map { elemToEpisode(it) }
    return Season(number, episodes)
}

private fun elemToEpisode(element: KElement): EpisodeInfo {
    val elemText = element.children().first().ownText()
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
        .first()
        .text()
        .replaceFirst("- ", "")
    val url = element
        .getElementsByTag("a")
        .first()
        .attr("href")
    return EpisodeInfo(url, number ?: 0, name, null) // TODO Overview
}