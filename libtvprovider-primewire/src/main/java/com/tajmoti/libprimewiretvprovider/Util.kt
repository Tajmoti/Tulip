package com.tajmoti.libprimewiretvprovider


/**
 * A function, which loads the provided URL into a real web browser and returns the page HTML source.
 */
typealias PageSourceLoader = suspend (
    url: String,
    urlBlocker: UrlBlocker,
    submitTriggerJsGenerator: (String) -> String
) -> Result<String>

/**
 * Same as [PageSourceLoader], but without the URL blocker.
 */
typealias SimplePageSourceLoader = suspend (url: String) -> Result<String>

/**
 * A predicate, which decides whether the provided URL should be loaded or not.
 * Used purely as an optimization to reduce useless network traffic during link extraction.
 */
typealias UrlBlocker = (String) -> Boolean