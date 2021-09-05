package com.tajmoti.libtvprovider.kinox

import com.tajmoti.commonutils.parallelMap
import com.tajmoti.libtvprovider.VideoStreamRef
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

private val REGEX_ANCHOR_URL = Regex("<a href=\"(.*)\" target=\"_blank\">")
private val REGEX_NAME = Regex("\"HosterName\":\"(.*?)\"")

internal suspend fun fetchSources(
    baseUrl: String,
    pageSource: String,
    pageSourceLoader: SimplePageSourceLoader
): Result<List<VideoStreamRef>> {
    return try {
        val links = Jsoup.parse(pageSource)
            .select("#HosterList")
            .first()!!
            .children()
        val result = links.parallelMap {
            runCatching { elementToStream(baseUrl, it, pageSourceLoader) }.getOrNull()
        }.filterNotNull()
        Result.success(result)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

private suspend fun elementToStream(
    baseUrl: String,
    li: Element,
    httpLoader: SimplePageSourceLoader
): VideoStreamRef? {
    val hoster = li.attr("rel")
    val url = "$baseUrl/aGET/Mirror/$hoster"
    val pageSource = httpLoader(url)
        .getOrElse { return null }
    val json = Jsoup.parse(pageSource)
        .body()
        .html()
        .replace("&quot;", "")
        .replace("\\", "")
    // Forgive me, father.
    val redirectUrl = REGEX_ANCHOR_URL.find(json)
        ?.groupValues
        ?.get(1) ?: return null
    val name = REGEX_NAME.find(json)
        ?.groupValues
        ?.get(1)
    return VideoStreamRef.Unresolved(name ?: "Unknown", redirectUrl)
}
