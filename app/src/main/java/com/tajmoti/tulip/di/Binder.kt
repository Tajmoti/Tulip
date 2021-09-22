package com.tajmoti.tulip.di

import com.tajmoti.libtulip.data.HostedInfoDataSource
import com.tajmoti.libtulip.data.LocalTvDataSource
import com.tajmoti.libtulip.data.UserDataDataSource
import com.tajmoti.libtulip.repository.*
import com.tajmoti.libtulip.repository.impl.*
import com.tajmoti.libtulip.service.LanguageMappingStreamService
import com.tajmoti.libtulip.service.VideoDownloadService
import com.tajmoti.libtulip.service.impl.LanguageMappingStreamServiceImpl
import com.tajmoti.tulip.datasource.AndroidHostedInfoDataSource
import com.tajmoti.tulip.datasource.AndroidTvDataSource
import com.tajmoti.tulip.datasource.AndroidUserDataDataSource
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
    fun bindHostedTvDataService(s: HostedTvDataRepositoryImpl): HostedTvDataRepository

    @Binds
    @Singleton
    fun provideVideoDownloader(s: DownloadManagerVideoDownloadService): VideoDownloadService

    @Binds
    @Singleton
    fun provideExtractionService(s: StreamsRepositoryImpl): StreamsRepository

    @Binds
    @Singleton
    fun provideLanguageMappingService(s: LanguageMappingStreamServiceImpl): LanguageMappingStreamService

    @Binds
    @Singleton
    fun provideAndroidTvShowRepository(s: AndroidHostedInfoDataSource): HostedInfoDataSource

    @Binds
    @Singleton
    fun provideLocalTvDataSource(s: AndroidTvDataSource): LocalTvDataSource

    @Binds
    @Singleton
    fun provideUserFavoriteDataRepository(s: AndroidUserDataDataSource): UserDataDataSource

    @Binds
    @Singleton
    fun provideTvDataRepository(s: TmdbTvDataRepositoryImpl): TmdbTvDataRepository

    @Binds
    @Singleton
    fun provideUserFavoritesService(s: FavoriteRepositoryImpl): FavoritesRepository

    @Binds
    @Singleton
    fun provideSubtitleRepository(s: SubtitleRepositoryImpl): SubtitleRepository

    @Binds
    @Singleton
    fun providePlayingHistoryRepository(s: PlayingHistoryRepositoryImpl): PlayingHistoryRepository
}