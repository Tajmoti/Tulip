package com.tajmoti.commonutils

import java.net.URLEncoder

actual object UrlEncoder {
    actual fun encode(url: String): String {
        return URLEncoder.encode(url, Charsets.UTF_8)
    }
}