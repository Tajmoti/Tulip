package com.tajmoti.ksoup

import org.jsoup.nodes.Document

actual class KDocument(private val impl: Document) : KElement(impl) {
    actual fun body(): KElement {
        return KElement(impl.body())
    }
}