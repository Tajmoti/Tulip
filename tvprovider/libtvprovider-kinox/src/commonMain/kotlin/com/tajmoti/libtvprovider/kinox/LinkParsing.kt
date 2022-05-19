package com.tajmoti.libtvprovider.kinox

import com.tajmoti.commonutils.PageSourceLoader
import com.tajmoti.commonutils.parallelMap
import com.tajmoti.ksoup.KDocument
import com.tajmoti.ksoup.KElement
import com.tajmoti.ksoup.KSoup
import com.tajmoti.libtvprovider.kinox.model.StreamReferenceObject
import com.tajmoti.libtvprovider.model.StreamingSiteLink
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

internal suspend fun fetchSources(
    baseUrl: String,
    pageSource: String,
    pageSourceLoader: PageSourceLoader
): Result<List<StreamingSiteLink>> {
    return try {
        val links = KSoup.parse(pageSource)
            .select("#HosterList")
            .first()
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
    li: KElement,
    httpLoader: PageSourceLoader
): StreamingSiteLink? {
    val hoster = li.attr("rel")
    val url = "$baseUrl/aGET/Mirror/$hoster"
    return httpLoader.loadWithGet(url, true)
        .map(::scrapeStreamRef)
        .getOrNull()
}

private fun scrapeStreamRef(pageSource: String): StreamingSiteLink? {
    val streamObject: StreamReferenceObject = Json { ignoreUnknownKeys = true }.decodeFromString(pageSource)
    val parsed = KSoup.parse(streamObject.stream)
    val name = streamObject.hosterName
    val videoUrl = extractVideoUrlFromResult(parsed) ?: return null
    return StreamingSiteLink(name, videoUrl)
}

private fun extractVideoUrlFromResult(parsed: KDocument): String? {
    val body = parsed.body()
    val anchorHref = body.getElementsByTag("a")
        .firstOrNull()
        ?.attr("href")
    val iframeSrc = body.getElementsByTag("iframe")
        .firstOrNull()
        ?.attr("src")
    var result = anchorHref ?: iframeSrc ?: return null
    if (!result.startsWith("http"))
        result = "https:$result"
    return result
}
