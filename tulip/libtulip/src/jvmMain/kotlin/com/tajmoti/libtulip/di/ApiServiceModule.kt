package com.tajmoti.libtulip.di

import com.tajmoti.libopensubtitles.OpenSubtitlesFallbackService
import com.tajmoti.libopensubtitles.OpenSubtitlesService
import com.tajmoti.libtmdb.TmdbService
import com.tajmoti.libtulip.TulipConfiguration
import com.tajmoti.libtulip.di.impl.ApiServiceModuleImpl
import com.tajmoti.libtulip.di.qualifier.RawHttpClient
import dagger.Module
import dagger.Provides
import io.ktor.client.*
import javax.inject.Singleton

@Module
object ApiServiceModule : IApiServiceModule {

    @Provides
    @Singleton
    override fun provideTmdbService(
        config: TulipConfiguration,
        @RawHttpClient ktor: HttpClient
    ): TmdbService {
        return ApiServiceModuleImpl.provideTmdbService(config, ktor)
    }

    @Provides
    @Singleton
    override fun provideOpenSubtitlesService(
        config: TulipConfiguration,
        @RawHttpClient ktor: HttpClient
    ): OpenSubtitlesService {
        return ApiServiceModuleImpl.provideOpenSubtitlesService(config, ktor)
    }

    @Provides
    @Singleton
    override fun provideOpenSubtitlesFallbackService(
        config: TulipConfiguration,
        @RawHttpClient ktor: HttpClient
    ): OpenSubtitlesFallbackService {
        return ApiServiceModuleImpl.provideOpenSubtitlesFallbackService(config, ktor)
    }
}
