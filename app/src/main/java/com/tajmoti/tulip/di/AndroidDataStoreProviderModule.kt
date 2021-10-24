package com.tajmoti.tulip.di

import android.content.Context
import androidx.room.Room
import com.tajmoti.tulip.db.AppDatabase
import com.tajmoti.tulip.db.TmdbDatabase
import com.tajmoti.tulip.db.UserDataDatabase
import com.tajmoti.tulip.db.dao.hosted.*
import com.tajmoti.tulip.db.dao.tmdb.TmdbDao
import com.tajmoti.tulip.db.dao.userdata.FavoritesDao
import com.tajmoti.tulip.db.dao.userdata.PlayingHistoryDao
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
    fun provideHostedDb(@ApplicationContext app: Context): AppDatabase {
        return Room.databaseBuilder(app, AppDatabase::class.java, "hosted.db").build()
    }

    @Provides
    @Singleton
    fun provideTmdbDb(@ApplicationContext app: Context): TmdbDatabase {
        return Room.databaseBuilder(app, TmdbDatabase::class.java, "tmdb.db").build()
    }

    @Provides
    @Singleton
    fun provideUserDataDb(@ApplicationContext app: Context): UserDataDatabase {
        return Room.databaseBuilder(app, UserDataDatabase::class.java, "userdata.db").build()
    }

    @Provides
    @Singleton
    fun provideTvShowDao(db: AppDatabase): TvShowDao {
        return db.tvShowDao()
    }

    @Provides
    @Singleton
    fun provideSeasonDao(db: AppDatabase): SeasonDao {
        return db.seasonDao()
    }

    @Provides
    @Singleton
    fun provideEpisodeDao(db: AppDatabase): EpisodeDao {
        return db.episodeDao()
    }

    @Provides
    @Singleton
    fun provideMovieDao(db: AppDatabase): MovieDao {
        return db.movieDao()
    }

    @Provides
    @Singleton
    fun provideTmdbMappingDao(db: AppDatabase): TmdbMappingDao {
        return db.tmdbMappingDao()
    }

    @Provides
    @Singleton
    fun provideTmdbDao(db: TmdbDatabase): TmdbDao {
        return db.tmdbDao()
    }

    @Provides
    @Singleton
    fun provideFavoritesDao(db: UserDataDatabase): FavoritesDao {
        return db.favoriteDao()
    }

    @Provides
    @Singleton
    fun providePlayingHistoryDao(db: UserDataDatabase): PlayingHistoryDao {
        return db.playingHistoryDao()
    }
}