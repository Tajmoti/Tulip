package com.tajmoti.libprimewiretvprovider

import com.tajmoti.ksoup.KElement
import com.tajmoti.ksoup.KSoup
import com.tajmoti.libtvprovider.model.VideoStreamRef

internal fun getVideoStreamsBlocking(html: String, baseUrl: String): Result<List<VideoStreamRef>> {
    return try {
        val streams = KSoup.parse(html)
            .getElementsByClass("movie_version")
            .filterNot { isAd(it) }
            .map { itemToStreamRef(it, baseUrl) }
        Result.success(streams)
    } catch (e: Throwable) {
        Result.failure(e)
    }
}

private fun itemToStreamRef(element: KElement, baseUrl: String): VideoStreamRef {
    val link = element
        .getElementsByClass("movie_version_link")
        .first()
        .getElementsByClass("propper-link")
        .first()
        .attr("href")
    val name = element
        .getElementsByClass("version-host")
        .first()
        .text()
    val redirectUrl = baseUrl + link
    return VideoStreamRef.Unresolved(name, redirectUrl)
}

private fun isAd(element: KElement): Boolean {
    return element.hasClass("nopop")
}