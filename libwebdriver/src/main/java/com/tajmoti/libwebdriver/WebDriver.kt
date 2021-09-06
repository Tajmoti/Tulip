package com.tajmoti.libwebdriver

interface WebDriver {

    /**
     * Loads the [url] into a real web browser; once the page load
     * callback is called, returns the HTML of the entire page.
     */
    suspend fun getPageHtml(url: String, params: Params): Result<String>

    data class Params(
        /**
         * Timeout after which the loading is aborted.
         */
        val timeoutMs: Long = 30_000,
        /**
         * Accepts or reject URLs, see [UrlFilter] doc.
         */
        val urlFilter: UrlFilter? = null
    )
}