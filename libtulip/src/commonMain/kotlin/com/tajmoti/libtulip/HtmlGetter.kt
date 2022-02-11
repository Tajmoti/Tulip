package com.tajmoti.libtulip

interface HtmlGetter {
    suspend fun getHtml(url: String): Result<String>
}