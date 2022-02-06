package com.tajmoti.multiplatform

import org.w3c.dom.url.URL

actual class Uri actual constructor(private val str: String) {
    actual val host: String
        get() = URL(str).host
}