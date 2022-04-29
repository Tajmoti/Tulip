package com.tajmoti.multiplatform

import java.net.URI

actual class Uri actual constructor(str: String) {
    private val impl = URI.create(str)
    actual val host = impl.host!!
}