package com.tajmoti.libprimewiretvprovider

import com.tajmoti.ksoup.KElement
import com.tajmoti.ksoup.KSoup
import com.tajmoti.libtvprovider.model.SearchResult
import com.tajmoti.libtvprovider.model.TvItemInfo

internal fun parseSearchResultPageBlocking(pageSource: String): List<SearchResult> {
    return KSoup.parse(pageSource)
        .getElementsByClass("index_container").first()
        .getElementsByClass("index_item")
        .map { elemToSearchResult(it) }
}

private fun elemToSearchResult(element: KElement): SearchResult {
    val anchor = element.getElementsByTag("a")
        .first()
    val itemUrl = anchor
        .attr("href")
    val isShow = itemUrl
        .startsWith("/tv/")
    val nameElem = anchor
        .getElementsByClass("title-cutoff")
        .first()
    val name = nameElem.ownText()
    val yearParen = nameElem.parent()!!
        .ownText()
    val year = yearParen.substring(1 until yearParen.length - 1)
    val yearInt = year.toIntOrNull()
    val info = TvItemInfo(name, "en", yearInt)
    return if (isShow) {
        SearchResult.TvShow(itemUrl, info)
    } else {
        SearchResult.Movie(itemUrl, info)
    }
}