package com.tajmoti.libtvvideoextractor

import com.tajmoti.commonutils.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Extracts a direct video URL from a streaming service video page.
 */
class VideoLinkExtractor(
    private val pageLoader: PageSourceLoaderWithLoadCount,
    private val modules: List<ExtractorModule> = ExtractorModule.DEFAULT_MODULES
) {

    /**
     * Attempts to extract a direct video link from the provided [url].
     */
    suspend fun extractVideoLink(url: String, serviceName: String?): Result<String> {
        logger.debug("Extracting '$url'")
        val handler = getFirstUsableHandler(url)
            ?: serviceName?.let { getFirstUsableHandlerByName(serviceName) }
        if (handler == null) {
            logger.debug("No handler found for '$url'")
            return Result.failure(Exception("No handler exists for url $url"))
        }
        return withContext(Dispatchers.Default) {
            handler.extractVideoUrl(url, pageLoader)
        }
    }

    /**
     * Returns true if [url] is supported for video link extraction.
     *
     * Note that it's still possible to fail video link extraction of a supported url
     * because of an exception or a change in the website's source code.
     */
    fun canExtractUrl(url: String): Boolean {
        return getFirstUsableHandler(url) != null
    }

    /**
     * Returns true if the service with [serviceName] is supported for video link extraction.
     * This check is performed as a best-effort and the name is simply comparet to known names.
     *
     * Note that it's still possible to fail video link extraction of a supported url
     * because of an exception or a change in the website's source code.
     */
    fun canExtractService(serviceName: String): Boolean {
        return getFirstUsableHandlerByName(serviceName) != null
    }

    private fun getFirstUsableHandler(url: String): ExtractorModule? {
        return modules.firstOrNull { module -> module.supportedUrls.any { url.contains(it) } }
    }

    private fun getFirstUsableHandlerByName(serviceName: String): ExtractorModule? {
        return modules.firstOrNull { module ->
            module.supportedServiceNames.any { it.equals(serviceName, true) }
        }
    }
}