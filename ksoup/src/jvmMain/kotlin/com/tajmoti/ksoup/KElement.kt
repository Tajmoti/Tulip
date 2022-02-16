package com.tajmoti.ksoup

import org.jsoup.nodes.Element

actual open class KElement(private val impl: Element) {
    actual fun attr(attributeKey: String): String {
        return impl.attr(attributeKey)
    }

    actual fun selectFirst(cssQuery: String): KElement? {
        return impl.selectFirst(cssQuery)?.let { KElement(it) }
    }

    actual fun getElementsByClass(className: String): List<KElement> {
        return impl.getElementsByClass(className).map { KElement(it) }
    }

    actual fun text(): String {
        return impl.text()
    }

    actual fun hasClass(className: String): Boolean {
        return impl.hasClass(className)
    }

    actual fun getElementsByTag(tag: String): List<KElement> {
        return impl.getElementsByTag(tag).map { KElement(it) }
    }

    actual fun ownText(): String {
        return impl.ownText()
    }

    actual fun parent(): KElement? {
        return impl.parent()?.let { KElement(it) }
    }

    actual fun previousElementSibling(): KElement? {
        return impl.previousElementSibling()?.let { KElement(it) }
    }

    actual fun select(cssQuery: String): List<KElement> {
        return impl.select(cssQuery).map { KElement(it) }
    }

    actual fun children(): List<KElement> {
        return impl.children().map { KElement(it) }
    }
}