package com.tajmoti.libtulip.misc

import com.tajmoti.commonutils.PageSourceLoader
import com.tajmoti.libwebdriver.TulipWebDriver
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.charsets.*

class TulipPageSourceLoader(
    private val driver: TulipWebDriver,
    private val proxy: HttpClient,
    private val local: HttpClient
) : PageSourceLoader {
    override suspend fun loadWithBrowser(
        url: String,
        filter: (String) -> Boolean,
        submitTriggerJsGenerator: (String) -> String
    ): Result<String> {
        val params = TulipWebDriver.Params(
            TulipWebDriver.SubmitTrigger.CustomJs(submitTriggerJsGenerator),
            urlFilter = filter
        )
        return driver.getPageHtml(url, params)
    }

    override suspend fun loadWithBrowser(url: String, filter: (String) -> Boolean): Result<String> {
        val params = TulipWebDriver.Params(
            TulipWebDriver.SubmitTrigger.OnPageLoaded,
            urlFilter = filter
        )
        return driver.getPageHtml(url, params)
    }

    override suspend fun loadWithGet(url: String): Result<String> {
        return loadWithKtor(proxy, url)
    }

    override suspend fun loadWithGetLocal(url: String): Result<String> {
        return loadWithKtor(local, url)
    }

    private suspend fun loadWithKtor(client: HttpClient, url: String): Result<String> {
        return try {
            Result.success(client.get(url).bodyAsText(Charsets.UTF_8))
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }
}