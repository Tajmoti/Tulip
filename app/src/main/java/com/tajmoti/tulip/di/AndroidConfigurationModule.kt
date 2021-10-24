package com.tajmoti.tulip.di

import com.tajmoti.libtulip.TulipConfiguration
import com.tajmoti.tulip.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.migration.DisableInstallInCheck
import javax.inject.Singleton

@Module
@DisableInstallInCheck
object AndroidConfigurationModule {
    @Provides
    @Singleton
    fun provideTulipConfiguration(): TulipConfiguration {
        return TulipConfiguration(
            tmdbCacheParams = TulipConfiguration.CacheParameters(60 * 60 * 1000L, 256),
            hostedItemCacheParams = TulipConfiguration.CacheParameters(60 * 60 * 1000L, 256),
            streamCacheParams = TulipConfiguration.CacheParameters(60 * 60 * 1000L, 256),
            tmdbApiKey = BuildConfig.tmdbApiKey,
            openSubtitlesApiKey = BuildConfig.openSubtitlesApiKey,
            debug = BuildConfig.DEBUG
        )
    }
}