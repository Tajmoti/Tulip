package com.tajmoti.tulip.di

import android.content.Context
import android.os.Handler
import androidx.room.Room
import com.tajmoti.commonutils.logger
import com.tajmoti.libprimewiretvprovider.PageSourceLoader
import com.tajmoti.libprimewiretvprovider.PrimewireTvProvider
import com.tajmoti.libtmdb.TmdbKeyInterceptor
import com.tajmoti.libtmdb.TmdbService
import com.tajmoti.libtulip.model.StreamingService
import com.tajmoti.libtvprovider.MultiTvProvider
import com.tajmoti.libtvprovider.kinox.KinoxTvProvider
import com.tajmoti.libtvvideoextractor.PageSourceLoaderWithLoadCount
import com.tajmoti.libtvvideoextractor.VideoLinkExtractor
import com.tajmoti.libwebdriver.WebDriver
import com.tajmoti.libwebdriver.WebViewWebDriver
import com.tajmoti.tulip.BuildConfig
import com.tajmoti.tulip.db.AppDatabase
import com.tajmoti.tulip.db.dao.EpisodeDao
import com.tajmoti.tulip.db.dao.MovieDao
import com.tajmoti.tulip.db.dao.SeasonDao
import com.tajmoti.tulip.db.dao.TvShowDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object Provider {

    @Provides
    @Singleton
    fun provideWebDriver(@ApplicationContext app: Context): WebDriver {
        val mainHandler = Handler(app.mainLooper)
        return WebViewWebDriver(app, mainHandler, blockImages = true)
    }

    @Provides
    @Singleton
    fun provideMultiTvProvider(webDriver: WebDriver): MultiTvProvider<StreamingService> {
        val webViewGetter = makeWebViewGetter(webDriver)
        val httpGetter = makeHttpGetter()
        val primewire = PrimewireTvProvider(webViewGetter, httpGetter)
        val kinox = KinoxTvProvider(httpGetter)
        return MultiTvProvider(
            mapOf(
                StreamingService.PRIMEWIRE to primewire,
                StreamingService.KINOX to kinox
            )
        )
    }

    /**
     * Returns a function, which loads the provided URL into a WebView,
     * runs all the JavaScript and returns the finished page HTML source.
     */
    private fun makeWebViewGetter(webDriver: WebDriver): PageSourceLoader {
        return { url, urlFilter ->
            val params = WebDriver.Params(urlFilter = urlFilter)
            webDriver.getPageHtml(url, params)
        }
    }

    /**
     * Returns a function, which performs a simple GET request asynchronously
     * and returns the loaded page's HTML source.
     */
    private fun makeHttpGetter(): suspend (url: String) -> Result<String> {
        val client = HttpClient(Android)
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
    fun provideHttpClient(): HttpClient {
        return HttpClient(Android) {
            followRedirects = false
            expectSuccess = false
        }
    }

    @Provides
    @Singleton
    fun provideLinkExtractor(webDriver: WebDriver): VideoLinkExtractor {
        val webViewGetter = makeWebViewGetterWithLoadCount(webDriver)
        return VideoLinkExtractor(webViewGetter)
    }

    /**
     * Same as [makeWebViewGetter], but the page must finish loading
     * count times before the page source is returned.
     */
    private fun makeWebViewGetterWithLoadCount(webDriver: WebDriver): PageSourceLoaderWithLoadCount {
        return { url, count, urlBlocker ->
            val params = WebDriver.Params(urlFilter = urlBlocker, count = count)
            webDriver.getPageHtml(url, params)
        }
    }


    @Provides
    @Singleton
    fun provideDb(@ApplicationContext app: Context): AppDatabase {
        return Room.databaseBuilder(app, AppDatabase::class.java, "tulip").build()
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
    fun provideTmdbService(): TmdbService {
        return createTmdbRetrofit().create(TmdbService::class.java)
    }

    private fun createTmdbRetrofit(): Retrofit {
        val builder = OkHttpClient.Builder()
            .addInterceptor(TmdbKeyInterceptor(BuildConfig.TMDB_API_KEY))
        if (BuildConfig.HTTP_DEBUG) {
            val logger = HttpLoggingInterceptor(interceptorLogger)
                .also { it.level = HttpLoggingInterceptor.Level.BODY }
            builder.addInterceptor(logger)
        }
        return Retrofit.Builder()
            .client(builder.build())
            .baseUrl("https://api.themoviedb.org/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    private val interceptorLogger = object : HttpLoggingInterceptor.Logger {
        override fun log(message: String) {
            logger.debug(message)
        }
    }
}
