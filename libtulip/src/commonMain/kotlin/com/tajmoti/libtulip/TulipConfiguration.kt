package com.tajmoti.libtulip

/**
 * Language used for all UI data, if available.
 */
const val PREFERRED_LANGUAGE = "en"

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
