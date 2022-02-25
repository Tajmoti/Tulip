package com.tajmoti.commonutils

interface PageSourceLoader {
    /**
     * Loads the provided [url] into a real web browser and returns the page HTML source.
     * A WebDriver is used to load the full page, useless URLs are blocked with [filter].
     * The page source is submitted only after [submitTriggerJsGenerator] JS code submits it.
     *
     * [filter] decides whether the provided URL should be loaded or not.
     * Used purely as an optimization to reduce useless network traffic and latency during link extraction.
     */
    suspend fun loadWithBrowser(url: String, filter: (String) -> Boolean, submitTriggerJsGenerator: (String) -> String): Result<String>

    /**
     * Loads the provided [url] into a real web browser and returns the page HTML source.
     * A WebDriver is used to load the full page, useless URLs are blocked with [filter].
     *
     * [filter] decides whether the provided URL should be loaded or not.
     * Used purely as an optimization to reduce useless network traffic and latency during link extraction.
     */
    suspend fun loadWithBrowser(url: String, filter: (String) -> Boolean): Result<String>

    /**
     * A raw HTTP GET request is performed to retrieve the HTML source for [url].
     * A proxy might be used, for example to get around CORS.
     */
    suspend fun loadWithGet(url: String): Result<String>

    /**
     * A raw HTTP GET request is performed to retrieve the HTML source for [url].
     * The request is guaranteed to be performed on the local machine (no proxy),
     * but the response might not be delivered because of CORS.
     */
    suspend fun loadWithGetLocal(url: String): Result<String>
}
