package com.tajmoti.tulip.di

import com.tajmoti.libtulip.repository.MovieRepository
import com.tajmoti.libtulip.repository.TvShowRepository
import com.tajmoti.tulip.repository.impl.AndroidMovieRepository
import com.tajmoti.tulip.repository.impl.AndroidTvShowRepository
import com.tajmoti.libtulip.service.StreamExtractorService
import com.tajmoti.libtulip.service.TvDataService
import com.tajmoti.libtulip.service.VideoDownloadService
import com.tajmoti.tulip.service.impl.DownloadManagerVideoDownloadService
import com.tajmoti.libtulip.service.impl.StreamsExtractionServiceImpl
import com.tajmoti.libtulip.service.impl.TvDataServiceImpl
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

    @Binds
    @Singleton
    fun provideAndroidTvShowRepository(s: AndroidTvShowRepository): TvShowRepository

    @Binds
    @Singleton
    fun provideAndroidMovieRepository(s: AndroidMovieRepository): MovieRepository
}