package com.tajmoti.libprimewiretvprovider

import com.tajmoti.libtvprovider.model.SearchResult
import com.tajmoti.libtvprovider.model.TvItemInfo
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

internal fun parseSearchResultPageBlocking(pageSource: String): Result<List<SearchResult>> {
    return try {
        val items = Jsoup.parse(pageSource)
            .getElementsByClass("index_item")
            .map { elemToSearchResult(it) }
        Result.success(items)
    } catch (e: Throwable) {
        Result.failure(e)
    }
}

private fun elemToSearchResult(element: Element): SearchResult {
    val anchor = element.getElementsByTag("a")
        .firstOrNull()!!
    val itemUrl = anchor
        .attr("href")
    val isShow = itemUrl
        .startsWith("/tv/")
    val nameElem = anchor
        .getElementsByClass("title-cutoff")
        .first()!!
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