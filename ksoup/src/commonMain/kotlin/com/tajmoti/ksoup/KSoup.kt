package com.tajmoti.ksoup

expect object KSoup {
    fun parse(html: String): KDocument
}