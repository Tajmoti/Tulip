package com.tajmoti.libtvvideoextractor

import arrow.core.Either
import com.tajmoti.libtvvideoextractor.module.StreamzzTo

/**
 * A module able to extract direct video links from one
 * or more video streaming sites.
 */
interface ExtractorModule {
    companion object {
        /**
         * All by-default included and supported link extractor modules.
         */
        val DEFAULT_MODULES = listOf(StreamzzTo())
    }

    /**
     * URLs that are supported for extraction by this module.
     */
    val supportedUrls: List<String>

    /**
     * List of streaming service names supported by this module.
     */
    val supportedServiceNames: List<String>


    suspend fun extractVideoUrl(
        url: String,
        rawLoader: RawPageSourceLoader,
        webDriverLoader: WebDriverPageSourceLoader
    ): Either<ExtractionError, String>
}