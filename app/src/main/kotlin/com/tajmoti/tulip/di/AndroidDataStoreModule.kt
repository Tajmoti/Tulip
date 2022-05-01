package com.tajmoti.tulip.di

import com.tajmoti.libtulip.data.*
import com.tajmoti.tulip.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.migration.DisableInstallInCheck
import javax.inject.Singleton

@Module
@DisableInstallInCheck
interface AndroidDataStoreModule {

    @Binds
    @Singleton
    fun provideAndroidHostedTvShowRepository(s: AndroidHostedTvShowRepository): HostedTvShowRepository

    @Binds
    @Singleton
    fun provideAndroidHostedSeasonRepository(s: AndroidHostedSeasonRepository): HostedSeasonRepository

    @Binds
    @Singleton
    fun provideAndroidHostedEpisodeRepository(s: AndroidHostedEpisodeRepository): HostedEpisodeRepository

    @Binds
    @Singleton
    fun provideAndroidHostedMovieRepository(s: AndroidHostedMovieRepository): HostedMovieRepository

    @Binds
    @Singleton
    fun provideTmdbTvShowRepository(s: AndroidTmdbTvShowRepository): TmdbTvShowRepository

    @Binds
    @Singleton
    fun provideTmdbSeasonRepository(s: AndroidTmdbSeasonRepository): TmdbSeasonRepository

    @Binds
    @Singleton
    fun provideTmdbEpisodeRepository(s: AndroidTmdbEpisodeRepository): TmdbEpisodeRepository

    @Binds
    @Singleton
    fun provideTmdbMovieRepository(s: AndroidTmdbMovieRepository): TmdbMovieRepository

    @Binds
    @Singleton
    fun provideUserFavoriteDataRepository(s: AndroidUserFavoriteRepository): UserFavoriteRepository

    @Binds
    @Singleton
    fun provideUserLastPlayedPositionRepository(s: AndroidUserLastPlayedPositionRepository): UserLastPlayedPositionRepository

    @Binds
    @Singleton
    fun provideTvShowMappingRepository(s: AndroidTvShowMappingRepository): TvShowMappingRepository

    @Binds
    @Singleton
    fun provideMovieMappingRepository(s: AndroidMovieMappingRepository): MovieMappingRepository
}