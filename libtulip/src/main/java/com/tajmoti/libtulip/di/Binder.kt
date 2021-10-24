package com.tajmoti.libtulip.di

import com.tajmoti.libtulip.repository.*
import com.tajmoti.libtulip.repository.impl.*
import com.tajmoti.libtulip.service.LanguageMappingStreamService
import com.tajmoti.libtulip.service.impl.LanguageMappingStreamServiceImpl
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
interface Binder {
    @Binds
    @Singleton
    fun bindHostedTvDataService(s: HostedTvDataRepositoryImpl): HostedTvDataRepository

    @Binds
    @Singleton
    fun provideExtractionService(s: StreamsRepositoryImpl): StreamsRepository

    @Binds
    @Singleton
    fun provideLanguageMappingService(s: LanguageMappingStreamServiceImpl): LanguageMappingStreamService

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