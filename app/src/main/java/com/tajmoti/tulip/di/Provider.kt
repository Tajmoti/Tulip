package com.tajmoti.tulip.di

import android.content.Context
import android.os.Handler
import androidx.room.Room
import com.tajmoti.libprimewiretvprovider.PageSourceLoader
import com.tajmoti.libprimewiretvprovider.PrimewireTvProvider
import com.tajmoti.libtvprovider.MultiTvProvider
import com.tajmoti.libtvprovider.kinox.KinoxTvProvider
import com.tajmoti.libtvvideoextractor.PageSourceLoaderWithLoadCount
import com.tajmoti.libtvvideoextractor.VideoLinkExtractor
import com.tajmoti.libwebdriver.WebDriver
import com.tajmoti.libwebdriver.WebViewWebDriver
import com.tajmoti.tulip.db.AppDatabase
import com.tajmoti.tulip.model.StreamingService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object Provider {

    @Provides
    @Singleton
    fun provideWebDriver(@ApplicationContext app: Context): WebDriver {
        val mainHandler = Handler(app.mainLooper)
        return WebViewWebDriver(app, mainHandler, blockImages = true)
    }

    @Provides
    @Singleton
    fun provideMultiTvProvider(webDriver: WebDriver): MultiTvProvider<StreamingService> {
        val webViewGetter = makeWebViewGetter(webDriver)
        val httpGetter = makeHttpGetter()
        val primewire = PrimewireTvProvider(webViewGetter, httpGetter)
        val kinox = KinoxTvProvider(httpGetter)
        return MultiTvProvider(
            mapOf(
                StreamingService.PRIMEWIRE to primewire,
                StreamingService.KINOX to kinox
            )
        )
    }

    /**
     * Returns a function, which loads the provided URL into a WebView,
     * runs all the JavaScript and returns the finished page HTML source.
     */
    private fun makeWebViewGetter(webDriver: WebDriver): PageSourceLoader {
        return { url, urlFilter ->
            val params = WebDriver.Params(urlFilter = urlFilter)
            webDriver.getPageHtml(url, params)
        }
    }

    /**
     * Returns a function, which performs a simple GET request asynchronously
     * and returns the loaded page's HTML source.
     */
    private fun makeHttpGetter(): suspend (url: String) -> Result<String> {
        val client = HttpClient(Android)
        return {
            try {
                Result.success(client.get(it))
            } catch (e: Throwable) {
                Result.failure(e)
            }
        }
    }

    @Provides
    @Singleton
    fun provideLinkExtractor(webDriver: WebDriver): VideoLinkExtractor {
        val webViewGetter = makeWebViewGetterWithLoadCount(webDriver)
        return VideoLinkExtractor(webViewGetter)
    }

    /**
     * Same as [makeWebViewGetter], but the page must finish loading
     * count times before the page source is returned.
     */
    private fun makeWebViewGetterWithLoadCount(webDriver: WebDriver): PageSourceLoaderWithLoadCount {
        return { url, count, urlBlocker ->
            val params = WebDriver.Params(urlFilter = urlBlocker, count = count)
            webDriver.getPageHtml(url, params)
        }
    }


    @Provides
    @Singleton
    fun provideDb(@ApplicationContext app: Context): AppDatabase {
        return Room.databaseBuilder(app, AppDatabase::class.java, "tulip").build()
    }
}
