package com.tajmoti.tulip.di

import com.tajmoti.libtulip.repository.*
import com.tajmoti.tulip.repository.AndroidMovieMappingRepository
import com.tajmoti.tulip.repository.AndroidTvShowMappingRepository
import com.tajmoti.tulip.repository.hosted.AndroidHostedEpisodeRepository
import com.tajmoti.tulip.repository.hosted.AndroidHostedMovieRepository
import com.tajmoti.tulip.repository.hosted.AndroidHostedSeasonRepository
import com.tajmoti.tulip.repository.hosted.AndroidHostedTvShowRepository
import com.tajmoti.tulip.repository.tmdb.AndroidTmdbEpisodeRepository
import com.tajmoti.tulip.repository.tmdb.AndroidTmdbMovieRepository
import com.tajmoti.tulip.repository.tmdb.AndroidTmdbSeasonRepository
import com.tajmoti.tulip.repository.tmdb.AndroidTmdbTvShowRepository
import com.tajmoti.tulip.repository.user.AndroidUserFavoriteRepository
import com.tajmoti.tulip.repository.user.AndroidUserLastPlayedPositionRepository
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