package com.tajmoti.tulip.di

import com.tajmoti.libtulip.data.LocalTvDataSource
import com.tajmoti.libtulip.data.UserDataDataSource
import com.tajmoti.libtulip.repository.HostedTvDataRepository
import com.tajmoti.libtulip.repository.TmdbTvDataRepository
import com.tajmoti.libtulip.repository.TmdbTvDataRepositoryImpl
import com.tajmoti.libtulip.service.*
import com.tajmoti.libtulip.service.impl.*
import com.tajmoti.tulip.repository.impl.AndroidHostedTvDataRepository
import com.tajmoti.tulip.repository.impl.AndroidTvDataSourceImpl
import com.tajmoti.tulip.repository.impl.AndroidUserDataDataSource
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
    fun provideAndroidTvShowRepository(s: AndroidHostedTvDataRepository): HostedTvDataRepository

    @Binds
    @Singleton
    fun provideLocalTvDataSource(s: AndroidTvDataSourceImpl): LocalTvDataSource

    @Binds
    @Singleton
    fun provideUserFavoriteDataRepository(s: AndroidUserDataDataSource): UserDataDataSource

    @Binds
    @Singleton
    fun provideTvDataRepository(s: TmdbTvDataRepositoryImpl): TmdbTvDataRepository

    @Binds
    @Singleton
    fun provideUserFavoritesService(s: UserFavoriteServiceImpl): UserFavoritesService
}