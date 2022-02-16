package com.tajmoti.ksoup

import org.jsoup.Jsoup

actual object KSoup {
    actual fun parse(html: String): KDocument {
        return KDocument(Jsoup.parse(html))
    }
}