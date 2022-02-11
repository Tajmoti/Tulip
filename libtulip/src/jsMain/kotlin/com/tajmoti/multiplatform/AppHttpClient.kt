package com.tajmoti.multiplatform

import com.tajmoti.libtulip.setupTulipKtor
import io.ktor.client.*
import io.ktor.client.engine.js.*

actual fun getAppHttpClient(): HttpClient {
    return HttpClient(Js, ::setupTulipKtor)
}