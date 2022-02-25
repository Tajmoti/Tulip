package com.tajmoti.multiplatform

import com.tajmoti.commonutils.UrlEncoder
import com.tajmoti.libtulip.misc.UrlRewriter
import com.tajmoti.libtulip.setupTulipKtor
import io.ktor.client.*
import io.ktor.client.engine.js.*
import org.koin.dsl.module

val jsNetworkModule = module {
    single { getAppHttpClient() }
}

fun getAppHttpClient(): HttpClient {
    return HttpClient(Js) {
        setupTulipKtor(this)
        install(UrlRewriter) { wrapper = ::wrapUrlInCorsProxy }
    }
}

private fun wrapUrlInCorsProxy(url: String): String {
    return "https://api.allorigins.win/raw?url=${UrlEncoder.encode(url)}"
}