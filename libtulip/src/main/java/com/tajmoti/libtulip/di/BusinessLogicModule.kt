package com.tajmoti.libtulip.di

import com.tajmoti.libprimewiretvprovider.PrimewireTvProvider
import com.tajmoti.libtulip.model.hosted.StreamingService
import com.tajmoti.libtulip.repository.HostedTvDataRepository
import com.tajmoti.libtulip.repository.StreamsRepository
import com.tajmoti.libtulip.repository.TmdbTvDataRepository
import com.tajmoti.libtulip.service.LanguageMappingStreamService
import com.tajmoti.libtulip.service.impl.LanguageMappingStreamServiceImpl
import com.tajmoti.libtvprovider.MultiTvProvider
import com.tajmoti.libtvprovider.kinox.KinoxTvProvider
import com.tajmoti.libtvvideoextractor.VideoLinkExtractor
import com.tajmoti.libtvvideoextractor.WebDriverPageSourceLoader
import com.tajmoti.libtvvideoextractor.WebDriverPageSourceLoaderWithCustomJs
import com.tajmoti.libwebdriver.WebDriver
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
    fun provideLanguageMappingService(
        hostedTvDataRepository: HostedTvDataRepository,
        streamsRepo: StreamsRepository,
        tvDataRepo: TmdbTvDataRepository
    ): LanguageMappingStreamService {
        return LanguageMappingStreamServiceImpl(hostedTvDataRepository, streamsRepo, tvDataRepo)
    }

    @Provides
    @Singleton
    fun provideMultiTvProvider(
        webDriver: WebDriver,
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
    fun provideLinkExtractor(okHttpClient: OkHttpClient, webDriver: WebDriver): VideoLinkExtractor {
        val webViewGetter = makeWebViewGetter(webDriver)
        val http = makeHttpGetter(okHttpClient)
        return VideoLinkExtractor(http, webViewGetter)
    }


    /**
     * Returns a function, which loads the provided URL into a WebView,
     * runs all the JavaScript and returns the finished page HTML source.
     */
    private fun makeWebViewGetterWithCustomJs(webDriver: WebDriver): WebDriverPageSourceLoaderWithCustomJs {
        return { url, urlFilter, interfaceName ->
            val params = WebDriver.Params(
                WebDriver.SubmitTrigger.CustomJs(interfaceName),
                urlFilter = urlFilter
            )
            webDriver.getPageHtml(url, params)
        }
    }

    /**
     * Returns a function, which loads the provided URL into a WebView,
     * runs all the JavaScript and returns the finished page HTML source.
     */
    private fun makeWebViewGetter(webDriver: WebDriver): WebDriverPageSourceLoader {
        return { url, urlFilter ->
            val params = WebDriver.Params(urlFilter = urlFilter)
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