package com.tajmoti.tulip.di

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.room.Room
import com.tajmoti.libopensubtitles.OpenSubtitlesFallbackService
import com.tajmoti.libopensubtitles.OpenSubtitlesService
import com.tajmoti.libtmdb.TmdbService
import com.tajmoti.libtulip.di.Provider
import com.tajmoti.libtulip.di.createAppOkHttpClient
import com.tajmoti.libtulip.di.createOpenSubtitlesFallbackRetrofit
import com.tajmoti.libtulip.di.createOpenSubtitlesRetrofit
import com.tajmoti.libtulip.di.createTmdbRetrofit
import com.tajmoti.libwebdriver.WebDriver
import com.tajmoti.libwebdriver.WebViewWebDriver
import com.tajmoti.tulip.BuildConfig
import com.tajmoti.tulip.db.AppDatabase
import com.tajmoti.tulip.db.TmdbDatabase
import com.tajmoti.tulip.db.UserDataDatabase
import com.tajmoti.tulip.db.dao.hosted.*
import com.tajmoti.tulip.db.dao.tmdb.TmdbDao
import com.tajmoti.tulip.db.dao.userdata.FavoritesDao
import com.tajmoti.tulip.db.dao.userdata.PlayingHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module(includes = [Provider::class])
object Provider {

    @Provides
    @Singleton
    fun provideWebDriver(@ApplicationContext app: Context): WebDriver {
        return WebViewWebDriver(app)
    }

    @Provides
    @Singleton
    fun provideDb(@ApplicationContext app: Context): AppDatabase {
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

    @Provides
    @Singleton
    fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
        return createAppOkHttpClient(context.cacheDir, { hasNetwork(context) }, BuildConfig.DEBUG)
    }

    @Provides
    @Singleton
    fun provideTmdbService(): TmdbService {
        return createTmdbRetrofit(BuildConfig.tmdbApiKey, BuildConfig.DEBUG)
            .create(TmdbService::class.java)
    }


    @Provides
    @Singleton
    fun provideOpenSubtitlesService(): OpenSubtitlesService {
        return createOpenSubtitlesRetrofit(BuildConfig.openSubtitlesApiKey, BuildConfig.DEBUG)
            .create(OpenSubtitlesService::class.java)
    }

    @Provides
    @Singleton
    fun provideOpenSubtitlesFallbackService(): OpenSubtitlesFallbackService {
        return createOpenSubtitlesFallbackRetrofit(BuildConfig.openSubtitlesApiKey,
            BuildConfig.DEBUG)
            .create(OpenSubtitlesFallbackService::class.java)
    }

    private fun hasNetwork(context: Context): Boolean {
        var isConnected = false //
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        if (activeNetwork != null && activeNetwork.isConnected)
            isConnected = true
        return isConnected
    }
}
