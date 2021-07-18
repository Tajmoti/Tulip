package com.tajmoti.tulip.di

import android.content.Context
import android.os.Handler
import com.tajmoti.libprimewiretvprovider.PrimewireTvProvider
import com.tajmoti.libtvprovider.TvProvider
import com.tajmoti.libtvvideoextractor.LinkExtractor
import com.tajmoti.libtvvideoextractor.LinkExtractorImpl
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
    fun providePageLoader(@ApplicationContext app: Context): PageLoader {
        val mainHandler = Handler(app.mainLooper)
        return WebViewPageLoader(app, mainHandler, blockImages = true)
    }

    @Provides
    @Singleton
    fun provideTvProvider(pageLoader: PageLoader): TvProvider {
        return PrimewireTvProvider({ a, b ->
            pageLoader.getPageHtml(a, 30000, b, 1).getOrThrow() // TODO
        })
    }

    @Provides
    @Singleton
    fun provideLinkExtractor(pageLoader: PageLoader): LinkExtractor {
        return LinkExtractorImpl { url, count, urlBlocker ->
            pageLoader.getPageHtml(url, 30000, urlBlocker, count).getOrThrow() // TODO
        }
    }
}
