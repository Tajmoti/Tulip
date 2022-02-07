package com.tajmoti.libtvprovider.model

sealed interface VideoStreamRef {
    /**
     * Name of the hosting site (informative).
     */
    val serviceName: String

    /**
     * URL to the streaming page (or a redirect in case this is [Unresolved]).
     */
    val url: String

    /**
     * A redirect to a streaming page.
     * Needs an HTML GET request to resolve the redirect.
     */
    data class Unresolved(
        override val serviceName: String,
        override val url: String,
    ) : VideoStreamRef

    /**
     * A real URL to the video streaming page.
     */
    data class Resolved(
        override val serviceName: String,
        override val url: String
    ) : VideoStreamRef
}