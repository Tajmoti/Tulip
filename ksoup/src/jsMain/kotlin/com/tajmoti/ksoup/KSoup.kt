package com.tajmoti.ksoup

import org.w3c.dom.parsing.DOMParser

actual object KSoup {
    actual fun parse(html: String): KDocument {
        return KDocument(DOMParser().parseFromString(html, "text/html"))
    }
}