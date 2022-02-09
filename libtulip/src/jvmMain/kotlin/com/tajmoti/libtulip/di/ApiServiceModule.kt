package com.tajmoti.libtulip.di

import com.tajmoti.libopensubtitles.OpenSubtitlesFallbackService
import com.tajmoti.libopensubtitles.OpenSubtitlesService
import com.tajmoti.libopensubtitles.RektorOpenSubtitlesFallbackService
import com.tajmoti.libopensubtitles.RektorOpenSubtitlesService
import com.tajmoti.libtmdb.RektorTmdbService
import com.tajmoti.libtmdb.TmdbService
import com.tajmoti.libtulip.TulipConfiguration
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
    fun provideOpenSubtitlesService(config: TulipConfiguration, ktor: HttpClient): OpenSubtitlesService {
        val queryParams = mapOf("Api-Key" to config.openSubtitlesApiKey)
        val rektor = KtorRektor(ktor, "https://api.opensubtitles.com/", queryParams)
        return RektorOpenSubtitlesService(LoggingRektor(rektor))
    }

    @Provides
    @Singleton
    fun provideOpenSubtitlesFallbackService(config: TulipConfiguration, ktor: HttpClient): OpenSubtitlesFallbackService {
        val queryParams = mapOf("Api-Key" to config.openSubtitlesApiKey)
        val rektor = KtorRektor(ktor, "https://www.opensubtitles.org/", queryParams)
        return RektorOpenSubtitlesFallbackService(LoggingRektor(rektor))
    }
}
