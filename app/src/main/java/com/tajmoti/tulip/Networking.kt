package com.tajmoti.tulip

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import com.tajmoti.commonutils.logger
import com.tajmoti.libtmdb.TmdbKeyInterceptor
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit


private const val MAX_REQUESTS = 32
private const val MAX_REQUESTS_PER_HOST = MAX_REQUESTS
private const val MAX_CACHE_STORAGE_TIME = 60 * 60 * 24 * 7
private const val MAX_CACHE_AGE = MAX_CACHE_STORAGE_TIME
private const val NETWORK_CACHE_SIZE_B = 5 * 1024 * 1024L

private val interceptorLogger = object : HttpLoggingInterceptor.Logger {
    override fun log(message: String) {
        logger.debug(message)
    }
}

fun createAppOkHttpClient(context: Context): OkHttpClient {
    val cacheSize = NETWORK_CACHE_SIZE_B
    val cache = Cache(context.cacheDir, cacheSize)
    val dispatcher = Dispatcher()
        .apply { maxRequestsPerHost = MAX_REQUESTS_PER_HOST }
        .apply { maxRequests = MAX_REQUESTS_PER_HOST }
    val pool = ConnectionPool(MAX_REQUESTS_PER_HOST, 5, TimeUnit.MINUTES)
    val builder = OkHttpClient.Builder()
        .retryOnConnectionFailure(false)
        .connectionPool(pool)
        .dispatcher(dispatcher)
        .cache(cache)
        .addInterceptor { chain -> makeCacheInterceptor(chain, context) }
        .addInterceptor(TmdbKeyInterceptor(BuildConfig.TMDB_API_KEY))
    if (BuildConfig.HTTP_DEBUG) {
        val logger = HttpLoggingInterceptor(interceptorLogger)
            .also { it.level = HttpLoggingInterceptor.Level.BODY }
        builder.addInterceptor(logger)
    }
    return builder.build()
}

private fun makeCacheInterceptor(chain: Interceptor.Chain, context: Context): Response {
    var request = chain.request()
    request = if (hasNetwork(context)) {
        request.newBuilder()
            .header("Cache-Control", "public, max-age=$MAX_CACHE_AGE")
            .build()
    } else {
        val maxCache = MAX_CACHE_STORAGE_TIME
        request.newBuilder()
            .header("Cache-Control", "public, only-if-cached, max-stale=$maxCache")
            .build()
    }
    return chain.proceed(request)
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