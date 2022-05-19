package com.tajmoti.libtvprovider.model

data class StreamingSiteLink(
    /**
     * Name of the hosting site (informative).
     */
    val serviceName: String,
    /**
     * URL to the streaming page.
     */
    val url: String,
)