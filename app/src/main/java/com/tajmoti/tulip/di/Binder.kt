package com.tajmoti.tulip.di

import com.tajmoti.tulip.service.StreamExtractorService
import com.tajmoti.tulip.service.TvDataService
import com.tajmoti.tulip.service.VideoDownloadService
import com.tajmoti.tulip.service.impl.DownloadManagerVideoDownloadService
import com.tajmoti.tulip.service.impl.StreamsExtractionServiceImpl
import com.tajmoti.tulip.service.impl.TvDataServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
interface Binder {

    @Binds
    @Singleton
    fun bindTvDataService(s: TvDataServiceImpl): TvDataService


    @Binds
    @Singleton
    fun provideVideoDownloader(s: DownloadManagerVideoDownloadService): VideoDownloadService

    @Binds
    @Singleton
    fun provideExtractionService(s: StreamsExtractionServiceImpl): StreamExtractorService
}