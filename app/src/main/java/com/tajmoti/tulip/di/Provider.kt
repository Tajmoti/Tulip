package com.tajmoti.tulip.di

import android.content.Context
import android.os.Handler
import androidx.room.Room
import com.tajmoti.libprimewiretvprovider.PrimewireTvProvider
import com.tajmoti.libtvprovider.MultiTvProvider
import com.tajmoti.libtvprovider.kinox.KinoxTvProvider
import com.tajmoti.libtvvideoextractor.VideoLinkExtractor
import com.tajmoti.libwebdriver.WebDriver
import com.tajmoti.libwebdriver.WebViewWebDriver
import com.tajmoti.tulip.db.AppDatabase
import com.tajmoti.tulip.model.StreamingService
import com.tajmoti.tulip.service.StreamExtractorService
import com.tajmoti.tulip.service.VideoDownloadService
import com.tajmoti.tulip.service.impl.DownloadManagerVideoDownloadService
import com.tajmoti.tulip.service.impl.StreamsExtractionServiceImpl
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
    fun provideMultiTvProvider(webDriver: WebDriver): MultiTvProvider<StreamingService> {
        val primewire = PrimewireTvProvider({ url, urlFilter ->
            val params = WebDriver.Params(urlFilter = urlFilter)
            webDriver.getPageHtml(url, params)
        })
        val kinox = KinoxTvProvider()
        return MultiTvProvider(
            mapOf(
                StreamingService.PRIMEWIRE to primewire,
                StreamingService.KINOX to kinox
            )
        )
    }

    @Provides
    @Singleton
    fun provideLinkExtractor(webDriver: WebDriver): VideoLinkExtractor {
        return VideoLinkExtractor({ url, count, urlBlocker ->
            val params = WebDriver.Params(urlFilter = urlBlocker, count = count)
            webDriver.getPageHtml(url, params)
        })
    }

    @Provides
    @Singleton
    fun provideDb(@ApplicationContext app: Context): AppDatabase {
        return Room.databaseBuilder(app, AppDatabase::class.java, "tulip").build()
    }

    @Provides
    @Singleton
    fun provideVideoDownloader(@ApplicationContext app: Context): VideoDownloadService {
        return DownloadManagerVideoDownloadService(app)
    }

    @Provides
    @Singleton
    fun provideExtractionService(linkExtractor: VideoLinkExtractor): StreamExtractorService {
        return StreamsExtractionServiceImpl(linkExtractor)
    }
}
