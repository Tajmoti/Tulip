package com.tajmoti.tulip.di

import com.tajmoti.libtulip.facade.VideoDownloadFacade
import com.tajmoti.libtulip.service.SubtitleService
import com.tajmoti.tulip.service.impl.DownloadManagerVideoDownloadFacade
import com.tajmoti.tulip.service.impl.SubtitleServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.migration.DisableInstallInCheck
import javax.inject.Singleton

@Module
@DisableInstallInCheck
interface AndroidBusinessLogicModule {

    @Binds
    @Singleton
    fun provideVideoDownloader(s: DownloadManagerVideoDownloadFacade): VideoDownloadFacade

    @Binds
    @Singleton
    fun provideSubtitleService(s: SubtitleServiceImpl): SubtitleService
}