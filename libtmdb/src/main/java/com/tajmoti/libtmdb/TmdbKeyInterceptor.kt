package com.tajmoti.libtmdb

import okhttp3.Interceptor
import okhttp3.Response

class TmdbKeyInterceptor(
    private val apiKey: String
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.call().request()
        if (!request.url().host().equals("api.themoviedb.org"))
            return chain.proceed(request)
        val newUrl = request.url().newBuilder()
            .addQueryParameter("api_key", apiKey)
            .build()
        val newRequest = request.newBuilder()
            .url(newUrl)
            .build()
        return chain.proceed(newRequest)
    }
}