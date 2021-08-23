package com.tajmoti.tulip.di

import android.content.Context
import android.os.Handler
import androidx.room.Room
import com.tajmoti.libprimewiretvprovider.PageSourceLoader
import com.tajmoti.libprimewiretvprovider.PrimewireTvProvider
import com.tajmoti.libtmdb.TmdbService
import com.tajmoti.libtulip.model.hosted.StreamingService
import com.tajmoti.libtvprovider.MultiTvProvider
import com.tajmoti.libtvprovider.kinox.KinoxTvProvider
import com.tajmoti.libtvvideoextractor.PageSourceLoaderWithLoadCount
import com.tajmoti.libtvvideoextractor.VideoLinkExtractor
import com.tajmoti.libwebdriver.WebDriver
import com.tajmoti.libwebdriver.WebViewWebDriver
import com.tajmoti.tulip.createAppOkHttpClient
import com.tajmoti.tulip.db.AppDatabase
import com.tajmoti.tulip.db.TmdbDatabase
import com.tajmoti.tulip.db.dao.hosted.EpisodeDao
import com.tajmoti.tulip.db.dao.hosted.MovieDao
import com.tajmoti.tulip.db.dao.hosted.SeasonDao
import com.tajmoti.tulip.db.dao.hosted.TvShowDao
import com.tajmoti.tulip.db.dao.tmdb.TmdbDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import okhttp3.OkHttpClient
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
    fun provideMultiTvProvider(
        webDriver: WebDriver,
        okHttpClient: OkHttpClient
    ): MultiTvProvider<StreamingService> {
        val webViewGetter = makeWebViewGetter(webDriver)
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
    fun provideTmdbDb(@ApplicationContext app: Context): TmdbDatabase {
        return Room.databaseBuilder(app, TmdbDatabase::class.java, "tmdb").build()
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
    fun provideTmdbDao(db: TmdbDatabase): TmdbDao {
        return db.tmdbDao()
    }

    @Provides
    @Singleton
    fun provideTmdbService(client: OkHttpClient): TmdbService {
        return createTmdbRetrofit(client).create(TmdbService::class.java)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
        return createAppOkHttpClient(context)
    }

    private fun createTmdbRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl("https://api.themoviedb.org/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }
}
