package com.tajmoti.libtulip.di

import com.tajmoti.commonutils.PageSourceLoader
import com.tajmoti.libwebdriver.TulipWebDriver
import io.ktor.client.*

interface INetworkingModule {
    /**
     * Returns a HTML source code getter.
     */
    fun makeHttpGetter(driver: TulipWebDriver, proxy: HttpClient, local: HttpClient): PageSourceLoader
}