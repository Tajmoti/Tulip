package com.tajmoti.libtulip

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

fun setupTulipKtor(c: HttpClientConfig<*>, followRedirects: Boolean = false) = with(c) {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
    this.followRedirects = followRedirects
    expectSuccess = false
}