package com.tajmoti.tulip.di

import android.content.Context
import android.os.Handler
import com.tajmoti.libprimewiretvprovider.PrimewireTvProvider
import com.tajmoti.libtvprovider.TvProvider
import com.tajmoti.libtvvideoextractor.VideoLinkExtractor
import com.tajmoti.libwebdriver.WebDriver
import com.tajmoti.libwebdriver.WebViewWebDriver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
    fun provideTvProvider(webDriver: WebDriver): TvProvider {
        return PrimewireTvProvider({ url, urlFilter ->
            val params = WebDriver.Params(urlFilter = urlFilter)
            webDriver.getPageHtml(url, params)
        })
    }

    @Provides
    @Singleton
    fun provideLinkExtractor(webDriver: WebDriver): VideoLinkExtractor {
        return VideoLinkExtractor({ url, count, urlBlocker ->
            val params = WebDriver.Params(urlFilter = urlBlocker, count = count)
            webDriver.getPageHtml(url, params)
        })
    }
}
