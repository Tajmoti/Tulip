package com.tajmoti.libtulip.di

import dagger.Module
import dagger.Provides
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
object NetworkingModule {
    @Provides
    @Singleton
    fun provideHttpClient(okHttpClient: OkHttpClient): HttpClient {
        return HttpClient(OkHttp) {
            engine { preconfigured = okHttpClient }
            followRedirects = false
            expectSuccess = false
        }
    }
}