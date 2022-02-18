package com.tajmoti.libtvprovider.kinox

import com.tajmoti.commonutils.logger
import com.tajmoti.commonutils.parallelMap
import com.tajmoti.ksoup.KDocument
import com.tajmoti.ksoup.KElement
import com.tajmoti.ksoup.KSoup
import com.tajmoti.libtvprovider.kinox.model.StreamReferenceObject
import com.tajmoti.libtvprovider.model.VideoStreamRef
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

internal suspend fun fetchSources(
    baseUrl: String,
    pageSource: String,
    pageSourceLoader: SimplePageSourceLoader
): Result<List<VideoStreamRef>> {
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
    httpLoader: SimplePageSourceLoader
): VideoStreamRef? {
    val hoster = li.attr("rel")
    val url = "$baseUrl/aGET/Mirror/$hoster"
    return httpLoader(url)
        .map(::scrapeStreamRef)
        .getOrNull()
}

private fun scrapeStreamRef(pageSource: String): VideoStreamRef.Unresolved? {
    val streamObject: StreamReferenceObject = Json { ignoreUnknownKeys = true }.decodeFromString(pageSource)
    val parsed = KSoup.parse(streamObject.stream)
    val name = streamObject.hosterName
    val videoUrl = extractVideoUrlFromResult(parsed) ?: return null
    return VideoStreamRef.Unresolved(name, videoUrl)
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
