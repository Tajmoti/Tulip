package com.tajmoti.libtulip.di

import com.tajmoti.libopensubtitles.OpenSubtitlesFallbackService
import com.tajmoti.libprimewiretvprovider.PrimewireTvProvider
import com.tajmoti.libtulip.model.hosted.StreamingService
import com.tajmoti.libtulip.repository.HostedTvDataRepository
import com.tajmoti.libtulip.repository.ItemMappingRepository
import com.tajmoti.libtulip.repository.TmdbTvDataRepository
import com.tajmoti.libtulip.service.MappingSearchService
import com.tajmoti.libtulip.service.StreamExtractionService
import com.tajmoti.libtulip.service.StreamService
import com.tajmoti.libtulip.service.SubtitleService
import com.tajmoti.libtulip.service.impl.MappingSearchServiceImpl
import com.tajmoti.libtulip.service.impl.StreamServiceImpl
import com.tajmoti.libtulip.service.impl.SubtitleServiceImpl
import com.tajmoti.libtvprovider.MultiTvProvider
import com.tajmoti.libtvprovider.kinox.KinoxTvProvider
import com.tajmoti.libtvvideoextractor.VideoLinkExtractor
import com.tajmoti.libtvvideoextractor.WebDriverPageSourceLoader
import com.tajmoti.libtvvideoextractor.WebDriverPageSourceLoaderWithCustomJs
import com.tajmoti.libwebdriver.TulipWebDriver
import dagger.Module
import dagger.Provides
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
object BusinessLogicModule {
    @Provides
    @Singleton
    fun provideStreamService(
        hostedTvDataRepository: HostedTvDataRepository,
        extractionService: StreamExtractionService,
        hostedToTmdbMappingRepository: ItemMappingRepository
    ): StreamService {
        return StreamServiceImpl(
            hostedTvDataRepository,
            extractionService,
            hostedToTmdbMappingRepository
        )
    }

    @Provides
    @Singleton
    fun provideSubtitleService(
        openSubtitlesFallbackService: OpenSubtitlesFallbackService
    ): SubtitleService {
        return SubtitleServiceImpl(openSubtitlesFallbackService)
    }

    @Provides
    @Singleton
    fun provideMappingSearchService(
        hostedRepository: HostedTvDataRepository,
        tmdbRepository: TmdbTvDataRepository,
        hostedToTmdbMappingRepository: ItemMappingRepository,
    ): MappingSearchService {
        return MappingSearchServiceImpl(
            hostedRepository,
            tmdbRepository,
            hostedToTmdbMappingRepository,
        )
    }

    @Provides
    @Singleton
    fun provideMultiTvProvider(
        webDriver: TulipWebDriver,
        okHttpClient: OkHttpClient,
    ): MultiTvProvider<StreamingService> {
        val webViewGetter = makeWebViewGetterWithCustomJs(webDriver)
        val httpGetter = makeHttpGetter(okHttpClient)
        val primewire = PrimewireTvProvider(webViewGetter, httpGetter)
        val kinox = KinoxTvProvider(httpGetter)
        return MultiTvProvider(
            mapOf(
                StreamingService.PRIMEWIRE to primewire,
                StreamingService.KINOX to kinox
            ),
            30_000L
        )
    }

    @Provides
    @Singleton
    fun provideLinkExtractor(okHttpClient: OkHttpClient, webDriver: TulipWebDriver): VideoLinkExtractor {
        val webViewGetter = makeWebViewGetter(webDriver)
        val http = makeHttpGetter(okHttpClient)
        return VideoLinkExtractor(http, webViewGetter)
    }


    /**
     * Returns a function, which loads the provided URL into a WebView,
     * runs all the JavaScript and returns the finished page HTML source.
     */
    private fun makeWebViewGetterWithCustomJs(webDriver: TulipWebDriver): WebDriverPageSourceLoaderWithCustomJs {
        return { url, urlFilter, interfaceName ->
            val params = TulipWebDriver.Params(
                TulipWebDriver.SubmitTrigger.CustomJs(interfaceName),
                urlFilter = urlFilter
            )
            webDriver.getPageHtml(url, params)
        }
    }

    /**
     * Returns a function, which loads the provided URL into a WebView,
     * runs all the JavaScript and returns the finished page HTML source.
     */
    private fun makeWebViewGetter(webDriver: TulipWebDriver): WebDriverPageSourceLoader {
        return { url, urlFilter ->
            val params = TulipWebDriver.Params(urlFilter = urlFilter)
            webDriver.getPageHtml(url, params)
        }
    }

    /**
     * Returns a function, which performs a simple GET request asynchronously
     * and returns the loaded page's HTML source.
     */
    private fun makeHttpGetter(okHttpClient: OkHttpClient): suspend (url: String) -> Result<String> {
        val client = HttpClient(OkHttp) {
            engine { preconfigured = okHttpClient }
        }
        return {
            try {
                Result.success(client.get(it))
            } catch (e: Throwable) {
                Result.failure(e)
            }
        }
    }
}