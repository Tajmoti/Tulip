package com.tajmoti.multiplatform

import com.tajmoti.commonutils.UrlEncoder

fun wrapUrlInCorsProxy(url: String): String {
    return "https://api.allorigins.win/raw?url=${UrlEncoder.encode(url)}"
}