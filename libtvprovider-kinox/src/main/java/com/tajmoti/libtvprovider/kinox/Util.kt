package com.tajmoti.libtvprovider.kinox

/**
 * Performs a GET request.
 */
typealias SimplePageSourceLoader = suspend (url: String) -> Result<String>
