package com.tajmoti.tulip.di

import com.tajmoti.libtulip.createAppOkHttpClient
import com.tajmoti.libwebdriver.TulipWebDriver
import com.tajmoti.tulip.driver.SeleniumWebDriver
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import java.io.File
import javax.inject.Singleton

@Module
object DesktopWebDriverModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return createAppOkHttpClient(File("cache/"), { true })
    }

    @Provides
    @Singleton
    fun provideWebDriver(): TulipWebDriver {
        return SeleniumWebDriver()
    }
}