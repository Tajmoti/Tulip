package com.tajmoti.libtulip.misc

import com.tajmoti.libwebdriver.TulipWebDriver
import com.tajmoti.multiplatform.getAppHttpClient
import io.ktor.client.request.*
import io.ktor.client.statement.*

class TulipJsWebDriver : TulipWebDriver {
    override suspend fun getPageHtml(url: String, params: TulipWebDriver.Params): Result<String> {
        return runCatching {
            val response: HttpResponse = getAppHttpClient().get(url)
            response.bodyAsText()
        }
    }
}