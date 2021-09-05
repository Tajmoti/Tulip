package com.tajmoti.libopensubtitles

import okhttp3.Interceptor
import okhttp3.Response

class OpenSubtitlesKeyInterceptor(
    private val apiKey: String
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.call().request()
        if (!request.url().host().equals("api.opensubtitles.com"))
            return chain.proceed(request)
        val newRequest = request.newBuilder()
            .addHeader("Api-Key", apiKey)
            .build()
        return chain.proceed(newRequest)
    }
}