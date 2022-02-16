package com.tajmoti.ksoup

expect open class KElement {
    fun attr(attributeKey: String): String

    fun selectFirst(cssQuery: String): KElement?

    fun getElementsByClass(className: String): List<KElement>

    fun text(): String

    fun ownText(): String

    fun hasClass(className: String): Boolean

    fun getElementsByTag(tag: String): List<KElement>

    fun parent(): KElement?

    fun previousElementSibling(): KElement?

    fun select(cssQuery: String): List<KElement>

    fun children(): List<KElement>
}