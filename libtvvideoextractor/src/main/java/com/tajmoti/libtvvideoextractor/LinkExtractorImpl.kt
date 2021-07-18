package com.tajmoti.libtvvideoextractor

import com.tajmoti.libtvvideoextractor.impl.StreamzzTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LinkExtractorImpl(
    private val pageLoader: PageSourceLoader
) : LinkExtractor {

    override suspend fun tryExtractLink(url: String): Result<String> {
        System.err.println("Extracting $url")
        val handler = getFirstUsableHandler(url)
            ?: return Result.failure(Exception("No handler exists for url $url"))
        return withContext(Dispatchers.IO) {
            return@withContext handler.extractVideoUrl(url, pageLoader)
        }
    }

    override fun canExtractLink(url: String): Boolean {
        return getFirstUsableHandler(url) != null
    }

    private fun getFirstUsableHandler(url: String): ExtractorModule? {
        return HANDLERS.firstOrNull { url.contains(it.supportedUrl) }
    }

    companion object {
        private val HANDLERS = listOf(
            StreamzzTo()
        )
    }
}