package com.tajmoti.multiplatform

import io.ktor.client.*

actual fun getAppHttpClient(): HttpClient {
    throw Error("This method should never be called on JVM!")
}