package com.tajmoti.libtulip.di

import com.tajmoti.libopensubtitles.OpenSubtitlesFallbackService
import com.tajmoti.libopensubtitles.OpenSubtitlesService
import com.tajmoti.libtmdb.TmdbService
import com.tajmoti.libtulip.TulipConfiguration
import com.tajmoti.libtulip.createOpenSubtitlesFallbackRetrofit
import com.tajmoti.libtulip.createOpenSubtitlesRetrofit
import com.tajmoti.libtulip.createTmdbRetrofit
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object ApiServiceModule {

    @Provides
    @Singleton
    fun provideTmdbService(config: TulipConfiguration): TmdbService {
        return createTmdbRetrofit(config.tmdbApiKey, config.httpDebug)
            .create(TmdbService::class.java)
    }

    @Provides
    @Singleton
    fun provideOpenSubtitlesService(config: TulipConfiguration): OpenSubtitlesService {
        return createOpenSubtitlesRetrofit(config.openSubtitlesApiKey, config.httpDebug)
            .create(OpenSubtitlesService::class.java)
    }

    @Provides
    @Singleton
    fun provideOpenSubtitlesFallbackService(config: TulipConfiguration): OpenSubtitlesFallbackService {
        return createOpenSubtitlesFallbackRetrofit(config.openSubtitlesApiKey, config.httpDebug)
            .create(OpenSubtitlesFallbackService::class.java)
    }
}
