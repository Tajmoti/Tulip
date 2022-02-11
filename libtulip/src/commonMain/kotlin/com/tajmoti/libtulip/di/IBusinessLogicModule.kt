package com.tajmoti.libtulip.di

import com.tajmoti.libopensubtitles.OpenSubtitlesFallbackService
import com.tajmoti.libtulip.HtmlGetter
import com.tajmoti.libtulip.model.hosted.StreamingService
import com.tajmoti.libtulip.repository.HostedTvDataRepository
import com.tajmoti.libtulip.repository.ItemMappingRepository
import com.tajmoti.libtulip.repository.TmdbTvDataRepository
import com.tajmoti.libtulip.service.MappingSearchService
import com.tajmoti.libtulip.service.StreamExtractionService
import com.tajmoti.libtulip.service.StreamService
import com.tajmoti.libtulip.service.SubtitleService
import com.tajmoti.libtvprovider.MultiTvProvider
import com.tajmoti.libtvvideoextractor.VideoLinkExtractor
import com.tajmoti.libtvvideoextractor.WebDriverPageSourceLoader
import com.tajmoti.libtvvideoextractor.WebDriverPageSourceLoaderWithCustomJs
import com.tajmoti.libwebdriver.TulipWebDriver
import io.ktor.client.*

interface IBusinessLogicModule {
    fun provideStreamService(
        hostedTvDataRepository: HostedTvDataRepository,
        extractionService: StreamExtractionService,
        hostedToTmdbMappingRepository: ItemMappingRepository
    ): StreamService

    fun provideSubtitleService(
        openSubtitlesFallbackService: OpenSubtitlesFallbackService
    ): SubtitleService

    fun provideMappingSearchService(
        hostedRepository: HostedTvDataRepository,
        tmdbRepository: TmdbTvDataRepository,
        hostedToTmdbMappingRepository: ItemMappingRepository,
    ): MappingSearchService

    fun provideMultiTvProvider(
        webDriver: TulipWebDriver,
        htmlGetter: HtmlGetter,
    ): MultiTvProvider<StreamingService>

    fun provideLinkExtractor(httpGetter: HtmlGetter, webDriver: TulipWebDriver): VideoLinkExtractor

    /**
     * Returns a function, which loads the provided URL into a WebView,
     * runs all the JavaScript and returns the finished page HTML source.
     */
    fun makeWebViewGetterWithCustomJs(webDriver: TulipWebDriver): WebDriverPageSourceLoaderWithCustomJs

    /**
     * Returns a function, which loads the provided URL into a WebView,
     * runs all the JavaScript and returns the finished page HTML source.
     */
    fun makeWebViewGetter(webDriver: TulipWebDriver): WebDriverPageSourceLoader

    /**
     * Returns a function, which performs a simple GET request asynchronously
     * and returns the loaded page's HTML source.
     */
    fun makeHttpGetter(client: HttpClient): HtmlGetter
}