package com.tajmoti.libtvprovider.kinox

import com.tajmoti.commonutils.parallelMap
import com.tajmoti.libtvprovider.model.VideoStreamRef
import com.tajmoti.libtvprovider.kinox.model.StreamReferenceObject
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

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
    return httpLoader(url)
        .map(::scrapeStreamRef)
        .getOrNull()
}

private fun scrapeStreamRef(pageSource: String): VideoStreamRef.Unresolved? {
    val streamObject: StreamReferenceObject = Json { ignoreUnknownKeys = true }.decodeFromString(pageSource)
    val parsed = Jsoup.parse(streamObject.stream)
    val name = streamObject.hosterName
    val videoUrl = extractVideoUrlFromResult(parsed) ?: return null
    return VideoStreamRef.Unresolved(name, videoUrl)
}

private fun extractVideoUrlFromResult(parsed: Document): String? {
    val body = parsed.body()
    val anchorHref = body.getElementsByTag("a")
        .firstOrNull()
        ?.attr("href")
    val iframeSrc = body.getElementsByTag("iframe")
        .firstOrNull()
        ?.attr("src")
    return anchorHref ?: iframeSrc
}
