package com.tajmoti.libtulip.di

import com.tajmoti.libtulip.di.qualifier.ProxyHttpClient
import com.tajmoti.libtulip.di.qualifier.RawHttpClient
import com.tajmoti.libtulip.misc.webdriver.UrlRewriter
import com.tajmoti.libtulip.setupTulipKtor
import com.tajmoti.multiplatform.wrapUrlInCorsProxy
import dagger.Module
import dagger.Provides
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
object NetworkingModule {
    @RawHttpClient
    @Provides
    @Singleton
    fun provideHttpClient(okHttpClient: OkHttpClient): HttpClient {
        return HttpClient(OkHttp) {
            engine { preconfigured = okHttpClient }
            setupTulipKtor(this)
        }
    }

    @ProxyHttpClient
    @Provides
    @Singleton
    fun provideProxyHttpClient(okHttpClient: OkHttpClient): HttpClient {
        return HttpClient(OkHttp) {
            engine { preconfigured = okHttpClient }
            setupTulipKtor(this)
            install(UrlRewriter) { wrapper = ::wrapUrlInCorsProxy }
        }
    }
}