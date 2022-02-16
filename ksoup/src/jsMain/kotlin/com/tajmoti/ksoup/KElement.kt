package com.tajmoti.ksoup

import org.w3c.dom.Element
import org.w3c.dom.asList

actual open class KElement(private val element: Element) {

    actual fun attr(attributeKey: String): String {
        return element.getAttribute(attributeKey) ?: ""
    }

    actual fun selectFirst(cssQuery: String): KElement? {
        return element.querySelector(cssQuery)?.let { KElement(it) }
    }

    actual fun getElementsByClass(className: String): List<KElement> {
        return element.getElementsByClassName(className).asList().map { KElement(it) }
    }

    actual fun text(): String {
        return element.textContent ?: ""
    }

    actual fun hasClass(className: String): Boolean {
        return element.classList.contains(className)
    }

    actual fun getElementsByTag(tag: String): List<KElement> {
        return element.getElementsByTagName(tag).asList().map { KElement(it) }
    }

    actual fun ownText(): String {
        return element.textContent ?: ""
    }

    actual fun parent(): KElement? {
        return element.parentElement?.let { KElement(it) }
    }

    actual fun previousElementSibling(): KElement? {
        TODO("Not yet implemented")
    }

    actual fun select(cssQuery: String): List<KElement> {
        return element.querySelectorAll(cssQuery).asList().map { KElement(it as Element) }
    }

    actual fun children(): List<KElement> {
        return element.children.asList().map { KElement(it) }
    }
}