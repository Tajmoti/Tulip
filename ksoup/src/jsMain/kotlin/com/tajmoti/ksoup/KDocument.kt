package com.tajmoti.ksoup

import org.w3c.dom.Document

actual class KDocument(private val document: Document) : KElement(document.documentElement!!) {

    actual fun body(): KElement {
        return KElement(document.body!!)
    }
}