package com.tajmoti.libtvprovider

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL


/**
 * Performs a GET request to the [url] and returns the value of the `location` response header.
 * If the header is missing, returns null.
 */
suspend fun resolveRedirects(url: String): Result<String?> {
    @Suppress("BlockingMethodInNonBlockingContext")
    return withContext(Dispatchers.IO) {
        runCatching {
            resolveRedirectsBlocking(url)
        }
    }
}

@Throws(IOException::class)
private fun resolveRedirectsBlocking(url: String): String? {
    val urlObj = URL(url)
    val con = urlObj.openConnection() as HttpURLConnection
    con.requestMethod = "GET"
    con.instanceFollowRedirects = false
    con.connect()
    val location = con.getHeaderField("location")
    con.disconnect()
    return location
}