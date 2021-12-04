package com.tajmoti.tulip.di

import com.tajmoti.libtulip.service.VideoDownloadService
import com.tajmoti.tulip.service.impl.DownloadManagerVideoDownloadService
import dagger.Binds
import dagger.Module
import dagger.hilt.migration.DisableInstallInCheck
import javax.inject.Singleton

@Module
@DisableInstallInCheck
interface AndroidBusinessLogicModule {
    @Binds
    @Singleton
    fun provideVideoDownloader(s: DownloadManagerVideoDownloadService): VideoDownloadService
}