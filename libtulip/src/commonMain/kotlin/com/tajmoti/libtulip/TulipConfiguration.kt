package com.tajmoti.libtulip

import com.tajmoti.libtulip.model.info.LanguageCode

/**
 * Language used for all UI data, if available.
 */
val PREFERRED_LANGUAGE = LanguageCode("en")

data class TulipConfiguration(
    val tmdbCacheParams: CacheParameters,
    val hostedItemCacheParams: CacheParameters,
    val streamCacheParams: CacheParameters,
    val tmdbApiKey: String,
    val openSubtitlesApiKey: String,
    val httpDebug: Boolean
) {
    data class CacheParameters(
        val validityMs: Long,
        val size: Int
    )
}
