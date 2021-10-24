package com.tajmoti.tulip.di

import com.tajmoti.libopensubtitles.OpenSubtitlesFallbackService
import com.tajmoti.libopensubtitles.OpenSubtitlesService
import com.tajmoti.libtmdb.TmdbService
import com.tajmoti.libtulip.di.createOpenSubtitlesFallbackRetrofit
import com.tajmoti.libtulip.di.createOpenSubtitlesRetrofit
import com.tajmoti.libtulip.di.createTmdbRetrofit
import com.tajmoti.tulip.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.migration.DisableInstallInCheck
import javax.inject.Singleton

@Module
@DisableInstallInCheck
object AndroidApiServiceModule {

    @Provides
    @Singleton
    fun provideTmdbService(): TmdbService {
        return createTmdbRetrofit(BuildConfig.tmdbApiKey, BuildConfig.DEBUG)
            .create(TmdbService::class.java)
    }


    @Provides
    @Singleton
    fun provideOpenSubtitlesService(): OpenSubtitlesService {
        return createOpenSubtitlesRetrofit(BuildConfig.openSubtitlesApiKey, BuildConfig.DEBUG)
            .create(OpenSubtitlesService::class.java)
    }

    @Provides
    @Singleton
    fun provideOpenSubtitlesFallbackService(): OpenSubtitlesFallbackService {
        return createOpenSubtitlesFallbackRetrofit(BuildConfig.openSubtitlesApiKey,
            BuildConfig.DEBUG)
            .create(OpenSubtitlesFallbackService::class.java)
    }
}
