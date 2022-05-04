@file:Suppress("DEPRECATION")
package com.tajmoti.tulip.di

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import com.tajmoti.commonutils.PageSourceLoader
import com.tajmoti.libtulip.createAppOkHttpClient
import com.tajmoti.libtulip.di.impl.NetworkingModuleImpl
import com.tajmoti.libtulip.di.qualifier.ProxyHttpClient
import com.tajmoti.libtulip.di.qualifier.RawHttpClientWithoutRedirects
import com.tajmoti.libwebdriver.TulipWebDriver
import com.tajmoti.libwebdriver.WebViewWebDriver
import com.tajmoti.tulip.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.migration.DisableInstallInCheck
import io.ktor.client.*
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@DisableInstallInCheck
object AndroidNetworkModule {
    @Provides
    @Singleton
    fun provideWebDriver(@ApplicationContext app: Context): TulipWebDriver {
        return WebViewWebDriver(app)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
        return createAppOkHttpClient(context.cacheDir, { hasNetwork(context) }, BuildConfig.HTTP_DEBUG)
    }

    @Provides
    @Singleton
    fun makeHttpGetter(driver: TulipWebDriver, @RawHttpClientWithoutRedirects client: HttpClient, @ProxyHttpClient proxyClient: HttpClient): PageSourceLoader {
        return NetworkingModuleImpl.makeHttpGetter(driver, proxyClient, client)
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