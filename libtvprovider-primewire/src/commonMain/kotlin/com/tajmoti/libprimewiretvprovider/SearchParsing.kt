package com.tajmoti.libprimewiretvprovider

import com.tajmoti.ksoup.KElement
import com.tajmoti.ksoup.KSoup
import com.tajmoti.libtvprovider.model.SearchResult
import com.tajmoti.libtvprovider.model.TvItemInfo

internal fun parseSearchResultPageBlocking(pageSource: String): Result<List<SearchResult>> {
    return try {
        val items = KSoup.parse(pageSource)
            .getElementsByClass("index_container").first()
            .getElementsByClass("index_item")
            .map { elemToSearchResult(it) }
        Result.success(items)
    } catch (e: Throwable) {
        Result.failure(e)
    }
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