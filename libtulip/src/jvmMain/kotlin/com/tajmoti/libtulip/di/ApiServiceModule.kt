package com.tajmoti.libtulip.di

import com.tajmoti.libopensubtitles.OpenSubtitlesFallbackService
import com.tajmoti.libopensubtitles.OpenSubtitlesService
import com.tajmoti.libtmdb.RektorTmdbService
import com.tajmoti.libtmdb.TmdbService
import com.tajmoti.libtulip.TulipConfiguration
import com.tajmoti.libtulip.createOpenSubtitlesFallbackRetrofit
import com.tajmoti.libtulip.createOpenSubtitlesRetrofit
import com.tajmoti.rektor.KtorRektor
import com.tajmoti.rektor.LoggingRektor
import dagger.Module
import dagger.Provides
import io.ktor.client.*
import javax.inject.Singleton

@Module
object ApiServiceModule {

    @Provides
    @Singleton
    fun provideTmdbService(config: TulipConfiguration, ktor: HttpClient): TmdbService {
        val queryParams = mapOf("api_key" to config.tmdbApiKey)
        val rektor = KtorRektor(ktor, "https://api.themoviedb.org/", queryParams)
        return RektorTmdbService(LoggingRektor(rektor))
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
