package com.tajmoti.multiplatform

import com.tajmoti.commonutils.UrlEncoder
import com.tajmoti.libtulip.di.ProxyType
import com.tajmoti.libtulip.misc.UrlRewriter
import com.tajmoti.libtulip.setupTulipKtor
import io.ktor.client.*
import io.ktor.client.engine.js.*
import org.koin.core.qualifier.qualifier
import org.koin.dsl.module

val jsNetworkModule = module {
    single { getAppHttpClient(proxy = true) }
    single(qualifier(ProxyType.PROXY)) { getAppHttpClient(proxy = true) }
    single(qualifier(ProxyType.DIRECT)) { getAppHttpClient(proxy = false) }
}

fun getAppHttpClient(proxy: Boolean): HttpClient {
    return HttpClient(Js) {
        setupTulipKtor(this)
        if (proxy) install(UrlRewriter) { wrapper = ::wrapUrlInCorsProxy }
    }
}

private fun wrapUrlInCorsProxy(url: String): String {
    return "https://api.allorigins.win/raw?url=${UrlEncoder.encode(url)}"
}