package com.tajmoti.libtvprovider

sealed class VideoStreamRef {
    /**
     * Name of the hosting site (informative).
     */
    abstract val serviceName: String

    /**
     * URL to the streaming page (or a redirect in case this is [Unresolved]).
     */
    abstract val url: String

    /**
     * A redirect to a streaming page.
     * Needs a HTML GET request to resolve the redirect.
     */
    data class Unresolved(
        override val serviceName: String,
        override val url: String,
    ) : VideoStreamRef() {

        fun asResolved(resolvedUrl: String): Resolved {
            return Resolved(serviceName, resolvedUrl)
        }
    }

    /**
     * A real URL to the video streaming page.
     */
    data class Resolved(
        override val serviceName: String,
        override val url: String
    ) : VideoStreamRef()
}