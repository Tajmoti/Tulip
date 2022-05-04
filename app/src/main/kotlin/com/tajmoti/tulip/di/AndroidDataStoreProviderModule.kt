package com.tajmoti.tulip.di

import android.content.Context
import androidx.room.Room
import com.tajmoti.tulip.TulipDatabase
import com.tajmoti.tulip.dao.ItemMappingDao
import com.tajmoti.tulip.dao.hosted.HostedEpisodeDao
import com.tajmoti.tulip.dao.hosted.HostedMovieDao
import com.tajmoti.tulip.dao.hosted.HostedSeasonDao
import com.tajmoti.tulip.dao.hosted.HostedTvShowDao
import com.tajmoti.tulip.dao.tmdb.TmdbEpisodeDao
import com.tajmoti.tulip.dao.tmdb.TmdbMovieDao
import com.tajmoti.tulip.dao.tmdb.TmdbSeasonDao
import com.tajmoti.tulip.dao.tmdb.TmdbTvShowDao
import com.tajmoti.tulip.dao.user.FavoriteDao
import com.tajmoti.tulip.dao.user.PlayingProgressDao
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.migration.DisableInstallInCheck
import javax.inject.Singleton

@Module
@DisableInstallInCheck
object AndroidDataStoreProviderModule {

    @Provides
    @Singleton
    fun provideTulipDatabase(@ApplicationContext app: Context): TulipDatabase {
        return Room.databaseBuilder(app, TulipDatabase::class.java, "tulip.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideTvShowDao(db: TulipDatabase): HostedTvShowDao {
        return db.hostedTvShowDao()
    }

    @Provides
    @Singleton
    fun provideSeasonDao(db: TulipDatabase): HostedSeasonDao {
        return db.hostedSeasonDao()
    }

    @Provides
    @Singleton
    fun provideEpisodeDao(db: TulipDatabase): HostedEpisodeDao {
        return db.hostedEpisodeDao()
    }

    @Provides
    @Singleton
    fun provideMovieDao(db: TulipDatabase): HostedMovieDao {
        return db.hostedMovieDao()
    }

    @Provides
    @Singleton
    fun provideTmdbMappingDao(db: TulipDatabase): ItemMappingDao {
        return db.tmdbMappingDao()
    }

    @Provides
    @Singleton
    fun provideTmdbTvShowDao(db: TulipDatabase): TmdbTvShowDao {
        return db.tmdbTvShowDao()
    }

    @Provides
    @Singleton
    fun provideTmdbSeasonDao(db: TulipDatabase): TmdbSeasonDao {
        return db.tmdbSeasonDao()
    }

    @Provides
    @Singleton
    fun provideTmdbEpisodeDao(db: TulipDatabase): TmdbEpisodeDao {
        return db.tmdbEpisodeDao()
    }

    @Provides
    @Singleton
    fun provideTmdbMovieDao(db: TulipDatabase): TmdbMovieDao {
        return db.tmdbMovieDao()
    }

    @Provides
    @Singleton
    fun provideFavoritesDao(db: TulipDatabase): FavoriteDao {
        return db.favoriteDao()
    }

    @Provides
    @Singleton
    fun providePlayingProgressDao(db: TulipDatabase): PlayingProgressDao {
        return db.playingProgressDao()
    }
}