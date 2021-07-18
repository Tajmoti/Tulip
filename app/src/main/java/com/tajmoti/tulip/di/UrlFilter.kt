package com.tajmoti.tulip.di

typealias UrlFilter = (url: String) -> Boolean

interface PageLoader {

    /**
     * Loads the [url] into a real web browser. Once the page loaded callback is called,
     * returns the HTML of the entire page.
     *
     * Use [urlFilter] to cancel loading of requests, which don't pass the predicate.
     * Return true to allow the request, false to block it.
     *
     * The [count] argument defines after how many onPageFinished calls should the HTML be submitted.
     *
     * This function must be called on the main thread of the application.
     */
    suspend fun getPageHtml(
        url: String,
        timeoutMs: Long,
        urlFilter: UrlFilter? = null,
        count: Int = 1
    ): Result<String>
}