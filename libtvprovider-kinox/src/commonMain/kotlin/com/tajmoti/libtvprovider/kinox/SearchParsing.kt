package com.tajmoti.libtvprovider.kinox

import com.tajmoti.ksoup.KElement
import com.tajmoti.ksoup.KSoup
import com.tajmoti.libtvprovider.model.SearchResult
import com.tajmoti.libtvprovider.model.TvItemInfo

internal fun parseSearchResultPageBlocking(
    pageSource: String,
    throwAwayItemsWithNoYear: Boolean
): Result<List<SearchResult>> {
    return try {
        val items = KSoup.parse(pageSource)
            .select("#RsltTableStatic > tbody:nth-child(2)")
            .first()
            .children()
            .mapNotNull { elemToSearchResult(it, throwAwayItemsWithNoYear) }
        Result.success(items)
    } catch (e: Throwable) {
        Result.failure(e)
    }
}

private fun elemToSearchResult(element: KElement, throwAwayItemsWithNoYear: Boolean): SearchResult? {
    val langIcon = element.children()[0].children()[0].attr("src")
    val languageNumber = langIcon
        .removeSuffix(".png")
        .replaceBeforeLast("/", "")
        .substring(1)
        .toInt()
    val language = languageNumberToLanguageCode(languageNumber)
    val type = element.children()[1].children()[0].attr("title")
    val titleElem = element.children()[2].children()[0]
    val title = titleElem.text()
    val link = titleElem.attr("href")
    val firstAirYear = element.getElementsByClass("Year")
        .first()
        .ownText()
        .toInt()
    if (throwAwayItemsWithNoYear && firstAirYear == 0)
        return null
    val info = TvItemInfo(title, language, firstAirYear)
    return when (type) {
        "series" -> SearchResult.TvShow(link, info)
        "movie" -> SearchResult.Movie(link, info)
        else -> null
    }
}

fun languageNumberToLanguageCode(number: Int): String {
    return when (number) {
        1 -> "de"
        else -> "en"
    }
}