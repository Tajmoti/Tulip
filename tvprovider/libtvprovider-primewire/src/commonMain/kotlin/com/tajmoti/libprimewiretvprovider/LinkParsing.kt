package com.tajmoti.libprimewiretvprovider

import com.tajmoti.ksoup.KElement
import com.tajmoti.ksoup.KSoup
import com.tajmoti.libtvprovider.model.StreamingSiteLink

internal fun getVideoStreamsBlocking(html: String, baseUrl: String): Result<List<StreamingSiteLink>> {
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

private fun itemToStreamRef(element: KElement, baseUrl: String): StreamingSiteLink {
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
    return StreamingSiteLink(name, redirectUrl)
}

private fun isAd(element: KElement): Boolean {
    return element.hasClass("nopop")
}