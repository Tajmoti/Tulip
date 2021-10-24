package com.tajmoti.tulip.di

import com.tajmoti.libtulip.service.VideoDownloadService
import com.tajmoti.tulip.service.DesktopVideoDownloadService
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object DesktopBusinessLogicModule {
    @Provides
    @Singleton
    fun provideVideoDownloader(): VideoDownloadService {
        return DesktopVideoDownloadService()
    }
}