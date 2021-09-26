package com.tajmoti.libtulip

data class TulipConfiguration(
    val tmdbCacheParams: CacheParameters,
    val hostedItemCacheParams: CacheParameters,
    val streamCacheParams: CacheParameters
) {
    data class CacheParameters(
        val validityMs: Long,
        val size: Int
    )
}
