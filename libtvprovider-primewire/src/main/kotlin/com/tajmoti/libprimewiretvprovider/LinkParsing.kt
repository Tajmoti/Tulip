package com.tajmoti.libprimewiretvprovider

import com.tajmoti.libtvprovider.VideoStreamRef
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

internal fun getVideoStreamsBlocking(html: String, baseUrl: String): Result<List<VideoStreamRef>> {
    return try {
        val streams = Jsoup.parse(html)
            .getElementsByClass("movie_version")
            .filterNot { isAd(it) }
            .map { itemToStreamRef(it, baseUrl) }
        Result.success(streams)
    } catch (e: Throwable) {
        Result.failure(e)
    }
}

private fun itemToStreamRef(element: Element, baseUrl: String): VideoStreamRef {
    val link = element
        .getElementsByClass("movie_version_link")
        .first()!!
        .getElementsByClass("propper-link")
        .attr("href")
    val name = element
        .getElementsByClass("version-host")
        .text()
    val redirectUrl = baseUrl + link
    return VideoStreamRef.Unresolved(name, redirectUrl)
}

private fun isAd(element: Element): Boolean {
    return element.hasClass("nopop")
}