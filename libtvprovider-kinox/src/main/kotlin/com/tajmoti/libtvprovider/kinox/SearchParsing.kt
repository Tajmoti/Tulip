package com.tajmoti.libtvprovider.kinox

import com.tajmoti.libtvprovider.SearchResult
import com.tajmoti.libtvprovider.TvItemInfo
import org.jsoup.Jsoup
import org.jsoup.nodes.Element


internal fun parseSearchResultPageBlocking(
    pageSource: String,
    throwAwayItemsWithNoYear: Boolean
): Result<List<SearchResult>> {
    return try {
        val items = Jsoup.parse(pageSource)
            .select("#RsltTableStatic > tbody:nth-child(2)")
            .first()!!
            .children()
            .mapNotNull { elemToSearchResult(it, throwAwayItemsWithNoYear) }
        Result.success(items)
    } catch (e: Throwable) {
        Result.failure(e)
    }
}

private fun elemToSearchResult(element: Element, throwAwayItemsWithNoYear: Boolean): SearchResult? {
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
    if (throwAwayItemsWithNoYear && firstAirYear == 0)
        return null
    val info = TvItemInfo(link, title, language, firstAirYear)
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