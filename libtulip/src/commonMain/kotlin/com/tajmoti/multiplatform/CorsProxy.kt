package com.tajmoti.multiplatform

import com.tajmoti.commonutils.UrlEncoder

fun wrapUrlInCorsProxy(url: String): String {
    return "https://api.allorigins.win/raw?charset=UTF-8&url=${UrlEncoder.encode(url)}"
}