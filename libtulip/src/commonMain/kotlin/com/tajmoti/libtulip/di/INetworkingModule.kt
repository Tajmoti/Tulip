package com.tajmoti.libtulip.di

import com.tajmoti.libtulip.HtmlGetter
import com.tajmoti.libtvvideoextractor.WebDriverPageSourceLoader
import com.tajmoti.libtvvideoextractor.WebDriverPageSourceLoaderWithCustomJs
import com.tajmoti.libwebdriver.TulipWebDriver
import io.ktor.client.*

interface INetworkingModule {
    /**
     * Returns a function, which loads the provided URL into a WebView,
     * runs all the JavaScript and returns the finished page HTML source.
     */
    fun makeWebViewGetterWithCustomJs(webDriver: TulipWebDriver): WebDriverPageSourceLoaderWithCustomJs

    /**
     * Returns a function, which loads the provided URL into a WebView,
     * runs all the JavaScript and returns the finished page HTML source.
     */
    fun makeWebViewGetter(webDriver: TulipWebDriver): WebDriverPageSourceLoader

    /**
     * Returns a function, which performs a simple GET request asynchronously
     * and returns the loaded page's HTML source.
     */
    fun makeHttpGetter(client: HttpClient): HtmlGetter
}