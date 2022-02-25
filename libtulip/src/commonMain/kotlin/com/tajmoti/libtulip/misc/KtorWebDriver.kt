package com.tajmoti.libtulip.misc

import com.tajmoti.libwebdriver.TulipWebDriver
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

class KtorWebDriver(private val client: HttpClient) : TulipWebDriver {
    override suspend fun getPageHtml(url: String, params: TulipWebDriver.Params): Result<String> {
        return runCatching {
            val response: HttpResponse = client.get(url)
            response.bodyAsText()
        }
    }
}