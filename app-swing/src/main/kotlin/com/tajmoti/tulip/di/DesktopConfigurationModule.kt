package com.tajmoti.tulip.di

import com.tajmoti.libtulip.TulipConfiguration
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object DesktopConfigurationModule {
    @Provides
    @Singleton
    fun provideTulipConfiguration(): TulipConfiguration {
        return TulipConfiguration(
            tmdbCacheParams = TulipConfiguration.CacheParameters(60 * 60 * 1000L, 256),
            hostedItemCacheParams = TulipConfiguration.CacheParameters(60 * 60 * 1000L, 256),
            streamCacheParams = TulipConfiguration.CacheParameters(60 * 60 * 1000L, 256),
            tmdbApiKey = System.getProperty("tmdbApiKey"),
            openSubtitlesApiKey = System.getProperty("openSubtitlesApiKey"),
            httpDebug = false
        )
    }
}