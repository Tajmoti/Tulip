package com.tajmoti.tulip.di

import com.tajmoti.libtulip.repository.*
import com.tajmoti.libtulip.service.*
import com.tajmoti.libtulip.service.impl.*
import com.tajmoti.tulip.repository.impl.AndroidHostedMovieRepository
import com.tajmoti.tulip.repository.impl.AndroidHostedTvShowRepository
import com.tajmoti.tulip.repository.impl.AndroidTvDataRepositoryImpl
import com.tajmoti.tulip.service.impl.DownloadManagerVideoDownloadService
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
    fun bindHostedTvDataService(s: HostedTvDataServiceImpl): HostedTvDataService

    @Binds
    @Singleton
    fun provideVideoDownloader(s: DownloadManagerVideoDownloadService): VideoDownloadService

    @Binds
    @Singleton
    fun provideExtractionService(s: StreamsExtractionServiceImpl): StreamExtractorService

    @Binds
    @Singleton
    fun provideSearchService(s: SearchServiceImpl): SearchService

    @Binds
    @Singleton
    fun provideLanguageMappingService(s: LanguageMappingStreamServiceImpl): LanguageMappingStreamService

    @Binds
    @Singleton
    fun provideAndroidTvShowRepository(s: AndroidHostedTvShowRepository): HostedTvShowRepository

    @Binds
    @Singleton
    fun provideAndroidMovieRepository(s: AndroidHostedMovieRepository): HostedMovieRepository

    @Binds
    @Singleton
    fun provideSearchableTvDataRepository(s: AndroidTvDataRepositoryImpl): WritableTvDataRepository

    @Binds
    @Singleton
    fun provideTvDataRepository(s: TvDataRepositoryImpl): SearchableTvDataRepository
}