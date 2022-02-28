package com.tajmoti.libtulip.di.impl

import com.tajmoti.commonutils.PageSourceLoader
import com.tajmoti.libtulip.di.INetworkingModule
import com.tajmoti.libtulip.misc.webdriver.TulipPageSourceLoader
import com.tajmoti.libwebdriver.TulipWebDriver
import io.ktor.client.*

object NetworkingModuleImpl : INetworkingModule {

    override fun makeHttpGetter(driver: TulipWebDriver, proxy: HttpClient, local: HttpClient): PageSourceLoader {
        return TulipPageSourceLoader(driver, proxy, local)
    }
}