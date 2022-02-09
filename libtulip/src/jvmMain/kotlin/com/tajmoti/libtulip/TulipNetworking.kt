package com.tajmoti.libtulip

import com.tajmoti.commonutils.logger
import com.tajmoti.libtulip.misc.net.UserAgentInterceptor
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.util.concurrent.TimeUnit

private const val MAX_REQUESTS = 4
private const val MAX_REQUESTS_PER_HOST = MAX_REQUESTS
private const val MAX_CACHE_STORAGE_TIME = 60 * 60 * 24 * 7
private const val MAX_CACHE_AGE = MAX_CACHE_STORAGE_TIME
private const val NETWORK_CACHE_SIZE_B = 5 * 1024 * 1024L
private const val USER_AGENT =
    "Mozilla/5.0 (Linux; Android 7.0; Pixel C Build/NRD90M; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/52.0.2743.98 Safari/537.36"

private val interceptorLogger = object : HttpLoggingInterceptor.Logger {
    override fun log(message: String) {
        logger.debug { message }
    }
}

fun createAppOkHttpClient(
    cacheDir: File,
    hasNetwork: () -> Boolean,
    debug: Boolean = false,
): OkHttpClient {
    val cacheSize = NETWORK_CACHE_SIZE_B
    val cache = Cache(cacheDir, cacheSize)
    val dispatcher = Dispatcher()
        .apply { maxRequestsPerHost = MAX_REQUESTS_PER_HOST }
        .apply { maxRequests = MAX_REQUESTS_PER_HOST }
    val pool = ConnectionPool(MAX_REQUESTS_PER_HOST, 5, TimeUnit.MINUTES)
    val builder = OkHttpClient.Builder()
        .retryOnConnectionFailure(false)
        .connectionPool(pool)
        .dispatcher(dispatcher)
        .cache(cache)
        .followRedirects(false)
        .addInterceptor { chain -> makeCacheInterceptor(chain, hasNetwork) }
        .addInterceptor(UserAgentInterceptor(USER_AGENT))
    if (debug) {
        val logger = HttpLoggingInterceptor(interceptorLogger)
            .also { it.level = HttpLoggingInterceptor.Level.BASIC }
        builder.addInterceptor(logger)
    }
    return builder.build()
}

private fun makeCacheInterceptor(chain: Interceptor.Chain, hasNetwork: () -> Boolean): Response {
    var request = chain.request()
    request = if (hasNetwork()) {
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