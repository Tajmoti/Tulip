package com.tajmoti.libtulip.misc

import com.tajmoti.libtulip.BuildKonfig
import com.tajmoti.libtulip.TulipConfiguration

object HardcodedConfigStore {
    val tulipConfiguration = TulipConfiguration(
        tmdbCacheParams = TulipConfiguration.CacheParameters(60 * 60 * 1000L, 256),
        hostedItemCacheParams = TulipConfiguration.CacheParameters(60 * 60 * 1000L, 256),
        streamCacheParams = TulipConfiguration.CacheParameters(60 * 60 * 1000L, 256),
        tmdbApiKey = BuildKonfig.tmdbApiKey,
        openSubtitlesApiKey = BuildKonfig.openSubtitlesApiKey,
        httpDebug = true
    )
}