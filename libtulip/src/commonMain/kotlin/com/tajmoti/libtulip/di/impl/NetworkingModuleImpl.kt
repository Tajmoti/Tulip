package com.tajmoti.libtulip.di.impl

import com.tajmoti.libtulip.HtmlGetter
import com.tajmoti.libtulip.di.INetworkingModule
import com.tajmoti.libtvvideoextractor.UrlBlocker
import com.tajmoti.libtvvideoextractor.WebDriverPageSourceLoader
import com.tajmoti.libtvvideoextractor.WebDriverPageSourceLoaderWithCustomJs
import com.tajmoti.libwebdriver.TulipWebDriver
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.charsets.*

object NetworkingModuleImpl : INetworkingModule {

    override fun makeWebViewGetterWithCustomJs(webDriver: TulipWebDriver): WebDriverPageSourceLoaderWithCustomJs {
        return object : WebDriverPageSourceLoaderWithCustomJs {
            override suspend fun load(
                url: String,
                urlBlocker: UrlBlocker,
                submitTriggerJsGenerator: (String) -> String
            ): Result<String> {
                val params = TulipWebDriver.Params(
                    TulipWebDriver.SubmitTrigger.CustomJs(submitTriggerJsGenerator),
                    urlFilter = urlBlocker
                )
                return webDriver.getPageHtml(url, params)
            }
        }
    }

    override fun makeWebViewGetter(webDriver: TulipWebDriver): WebDriverPageSourceLoader {
        return object : WebDriverPageSourceLoader {
            override suspend fun load(url: String, urlBlocker: UrlBlocker): Result<String> {
                val params = TulipWebDriver.Params(urlFilter = urlBlocker)
                return webDriver.getPageHtml(url, params)
            }
        }
    }

    override fun makeHttpGetter(client: HttpClient): HtmlGetter {
        return object : HtmlGetter {
            override suspend fun getHtml(url: String): Result<String> {
                return try {
                    Result.success(client.get(url).bodyAsText(Charsets.UTF_8))
                } catch (e: Throwable) {
                    Result.failure(e)
                }
            }
        }
    }
}