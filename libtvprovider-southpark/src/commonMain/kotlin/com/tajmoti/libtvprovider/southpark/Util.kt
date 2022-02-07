package com.tajmoti.libtvprovider.southpark

/**
 * Performs a GET request.
 */
typealias SimplePageSourceLoader = suspend (url: String) -> Result<String>
