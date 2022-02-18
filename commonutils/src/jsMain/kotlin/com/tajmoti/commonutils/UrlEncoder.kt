package com.tajmoti.commonutils

private external fun encodeURIComponent(s: String): String

actual object UrlEncoder {
    actual fun encode(url: String): String {
        return encodeURIComponent(url)
    }
}