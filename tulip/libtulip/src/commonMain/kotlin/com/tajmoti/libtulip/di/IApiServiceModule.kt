package com.tajmoti.libtulip.di

import com.tajmoti.libopensubtitles.OpenSubtitlesFallbackService
import com.tajmoti.libopensubtitles.OpenSubtitlesService
import com.tajmoti.libtmdb.TmdbService
import com.tajmoti.libtulip.TulipConfiguration
import io.ktor.client.*

interface IApiServiceModule {
    fun provideTmdbService(config: TulipConfiguration, ktor: HttpClient): TmdbService

    fun provideOpenSubtitlesService(config: TulipConfiguration, ktor: HttpClient): OpenSubtitlesService

    fun provideOpenSubtitlesFallbackService(config: TulipConfiguration, ktor: HttpClient): OpenSubtitlesFallbackService
}