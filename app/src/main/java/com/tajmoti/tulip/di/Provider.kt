package com.tajmoti.tulip.di

import android.content.Context
import androidx.room.Room
import com.tajmoti.libopensubtitles.OpenSubtitlesFallbackService
import com.tajmoti.libopensubtitles.OpenSubtitlesService
import com.tajmoti.libprimewiretvprovider.PrimewireTvProvider
import com.tajmoti.libtmdb.TmdbService
import com.tajmoti.libtulip.TulipConfiguration
import com.tajmoti.libtulip.model.hosted.StreamingService
import com.tajmoti.libtvprovider.MultiTvProvider
import com.tajmoti.libtvprovider.kinox.KinoxTvProvider
import com.tajmoti.libtvvideoextractor.VideoLinkExtractor
import com.tajmoti.libtvvideoextractor.WebDriverPageSourceLoader
import com.tajmoti.libtvvideoextractor.WebDriverPageSourceLoaderWithCustomJs
import com.tajmoti.libwebdriver.WebDriver
import com.tajmoti.libwebdriver.WebViewWebDriver
import com.tajmoti.tulip.createAppOkHttpClient
import com.tajmoti.tulip.createOpenSubtitlesFallbackRetrofit
import com.tajmoti.tulip.createOpenSubtitlesRetrofit
import com.tajmoti.tulip.createTmdbRetrofit
import com.tajmoti.tulip.db.AppDatabase
import com.tajmoti.tulip.db.TmdbDatabase
import com.tajmoti.tulip.db.UserDataDatabase
import com.tajmoti.tulip.db.dao.hosted.*
import com.tajmoti.tulip.db.dao.tmdb.TmdbDao
import com.tajmoti.tulip.db.dao.userdata.FavoritesDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import okhttp3.OkHttpClient
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object Provider {

    @Provides
    @Singleton
    fun provideWebDriver(@ApplicationContext app: Context): WebDriver {
        return WebViewWebDriver(app)
    }

    @Provides
    @Singleton
    fun provideMultiTvProvider(
        webDriver: WebDriver,
        okHttpClient: OkHttpClient
    ): MultiTvProvider<StreamingService> {
        val webViewGetter = makeWebViewGetterWithCustomJs(webDriver)
        val httpGetter = makeHttpGetter(okHttpClient)
        val primewire = PrimewireTvProvider(webViewGetter, httpGetter)
        val kinox = KinoxTvProvider(httpGetter)
        return MultiTvProvider(
            mapOf(
                StreamingService.PRIMEWIRE to primewire,
                StreamingService.KINOX to kinox
            ),
            30_000L
        )
    }

    /**
     * Returns a function, which loads the provided URL into a WebView,
     * runs all the JavaScript and returns the finished page HTML source.
     */
    private fun makeWebViewGetter(webDriver: WebDriver): WebDriverPageSourceLoader {
        return { url, urlFilter ->
            val params = WebDriver.Params(urlFilter = urlFilter)
            webDriver.getPageHtml(url, params)
        }
    }

    /**
     * Returns a function, which loads the provided URL into a WebView,
     * runs all the JavaScript and returns the finished page HTML source.
     */
    private fun makeWebViewGetterWithCustomJs(webDriver: WebDriver): WebDriverPageSourceLoaderWithCustomJs {
        return { url, urlFilter, interfaceName ->
            val params = WebDriver.Params(
                WebDriver.SubmitTrigger.CustomJs(interfaceName),
                urlFilter = urlFilter
            )
            webDriver.getPageHtml(url, params)
        }
    }

    /**
     * Returns a function, which performs a simple GET request asynchronously
     * and returns the loaded page's HTML source.
     */
    private fun makeHttpGetter(okHttpClient: OkHttpClient): suspend (url: String) -> Result<String> {
        val client = HttpClient(OkHttp) {
            engine { preconfigured = okHttpClient }
        }
        return {
            try {
                Result.success(client.get(it))
            } catch (e: Throwable) {
                Result.failure(e)
            }
        }
    }

    @Provides
    @Singleton
    fun provideHttpClient(okHttpClient: OkHttpClient): HttpClient {
        return HttpClient(OkHttp) {
            engine { preconfigured = okHttpClient }
            followRedirects = false
            expectSuccess = false
        }
    }

    @Provides
    @Singleton
    fun provideLinkExtractor(okHttpClient: OkHttpClient, webDriver: WebDriver): VideoLinkExtractor {
        val webViewGetter = makeWebViewGetter(webDriver)
        val http = makeHttpGetter(okHttpClient)
        return VideoLinkExtractor(http, webViewGetter)
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
    fun provideTmdbService(): TmdbService {
        return createTmdbRetrofit().create(TmdbService::class.java)
    }

    @Provides
    @Singleton
    fun provideOpenSubtitlesService(): OpenSubtitlesService {
        return createOpenSubtitlesRetrofit().create(OpenSubtitlesService::class.java)
    }

    @Provides
    @Singleton
    fun provideOpenSubtitlesFallbackService(): OpenSubtitlesFallbackService {
        return createOpenSubtitlesFallbackRetrofit().create(OpenSubtitlesFallbackService::class.java)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
        return createAppOkHttpClient(context)
    }

    @Provides
    @Singleton
    fun provideTulipConfiguration(): TulipConfiguration {
        return TulipConfiguration(
            tmdbCacheParams = TulipConfiguration.CacheParameters(60 * 60 * 1000L, 256),
            hostedItemCacheParams = TulipConfiguration.CacheParameters(60 * 60 * 1000L, 256),
            streamCacheParams = TulipConfiguration.CacheParameters(60 * 60 * 1000L, 256)
        )
    }
}
