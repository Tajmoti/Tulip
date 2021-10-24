package com.tajmoti.libtulip

data class TulipConfiguration(
    val tmdbCacheParams: CacheParameters,
    val hostedItemCacheParams: CacheParameters,
    val streamCacheParams: CacheParameters,
    val tmdbApiKey: String,
    val openSubtitlesApiKey: String,
    val debug: Boolean
) {
    data class CacheParameters(
        val validityMs: Long,
        val size: Int
    )
}
