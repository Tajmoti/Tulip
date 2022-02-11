package com.tajmoti.libtulip

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

fun setupTulipKtor(c: HttpClientConfig<*>) = with(c) {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
    followRedirects = false
    expectSuccess = false
}