package com.tajmoti.libtvvideoextractor


/**
 * A function, which loads the provided URL into a real web browser and returns the page HTML source.
 * A WebDriver is used to load the full page, useless URLs are blocked with urlBlocker.
 */
typealias WebDriverPageSourceLoader = suspend (url: String, urlBlocker: UrlBlocker) -> Result<String>

/**
 * A function, which loads the provided URL into a real web browser and returns the page HTML source.
 * A WebDriver is used to load the full page, useless URLs are blocked with urlBlocker.
 */
typealias WebDriverPageSourceLoaderWithCustomJs = suspend (
    url: String,
    urlBlocker: UrlBlocker,
    submitTriggerJsGenerator: (String) -> String
) -> Result<String>

/**
 * A function, which loads the provided URL into a real web browser and returns the page HTML source.
 * A raw HTTP GET request is performed to retrieve the HTML source.
 */
typealias RawPageSourceLoader = suspend (url: String) -> Result<String>

/**
 * A predicate, which decides whether the provided URL should be loaded or not.
 * Used purely as an optimization to reduce useless network traffic during link extraction.
 */
typealias UrlBlocker = (String) -> Boolean