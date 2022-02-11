package com.tajmoti.libtulip.di

import com.tajmoti.libopensubtitles.OpenSubtitlesFallbackService
import com.tajmoti.libtulip.HtmlGetter
import com.tajmoti.libtulip.di.impl.BusinessLogicModuleImpl
import com.tajmoti.libtulip.model.hosted.StreamingService
import com.tajmoti.libtulip.repository.HostedTvDataRepository
import com.tajmoti.libtulip.repository.ItemMappingRepository
import com.tajmoti.libtulip.repository.TmdbTvDataRepository
import com.tajmoti.libtulip.service.StreamExtractionService
import com.tajmoti.libtvprovider.MultiTvProvider
import com.tajmoti.libwebdriver.TulipWebDriver
import dagger.Module
import dagger.Provides
import io.ktor.client.*
import javax.inject.Singleton

@Module
object BusinessLogicModule : IBusinessLogicModule {
    @Provides
    @Singleton
    override fun provideStreamService(
        hostedTvDataRepository: HostedTvDataRepository,
        extractionService: StreamExtractionService,
        hostedToTmdbMappingRepository: ItemMappingRepository
    ) = BusinessLogicModuleImpl.provideStreamService(
        hostedTvDataRepository,
        extractionService,
        hostedToTmdbMappingRepository
    )

    @Provides
    @Singleton
    override fun provideSubtitleService(
        openSubtitlesFallbackService: OpenSubtitlesFallbackService
    ) = BusinessLogicModuleImpl.provideSubtitleService(openSubtitlesFallbackService)

    @Provides
    @Singleton
    override fun provideMappingSearchService(
        hostedRepository: HostedTvDataRepository,
        tmdbRepository: TmdbTvDataRepository,
        hostedToTmdbMappingRepository: ItemMappingRepository,
    ) = BusinessLogicModuleImpl.provideMappingSearchService(
        hostedRepository,
        tmdbRepository,
        hostedToTmdbMappingRepository
    )

    @Provides
    @Singleton
    override fun provideMultiTvProvider(
        webDriver: TulipWebDriver,
        htmlGetter: HtmlGetter,
    ): MultiTvProvider<StreamingService> {
        return BusinessLogicModuleImpl.provideMultiTvProvider(webDriver, htmlGetter)
    }

    @Provides
    @Singleton
    override fun provideLinkExtractor(httpGetter: HtmlGetter, webDriver: TulipWebDriver) =
        BusinessLogicModuleImpl.provideLinkExtractor(httpGetter, webDriver)

    @Provides
    @Singleton
    override fun makeWebViewGetterWithCustomJs(webDriver: TulipWebDriver) =
        BusinessLogicModuleImpl.makeWebViewGetterWithCustomJs(webDriver)

    @Provides
    @Singleton
    override fun makeWebViewGetter(webDriver: TulipWebDriver) =
        BusinessLogicModuleImpl.makeWebViewGetter(webDriver)

    @Provides
    @Singleton
    override fun makeHttpGetter(client: HttpClient) =
        BusinessLogicModuleImpl.makeHttpGetter(client)
}