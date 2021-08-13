package com.tajmoti.libtvvideoextractor


/**
 * A function, which loads the provided URL into a real web browser and returns the page HTML source.
 */
typealias PageSourceLoaderWithLoadCount = suspend (url: String, count: Int, urlBlocker: UrlBlocker) -> Result<String>

/**
 * A predicate, which decides whether the provided URL should be loaded or not.
 * Used purely as an optimization to reduce useless network traffic during link extraction.
 */
typealias UrlBlocker = (String) -> Boolean