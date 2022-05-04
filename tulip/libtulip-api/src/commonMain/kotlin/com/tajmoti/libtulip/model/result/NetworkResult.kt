package com.tajmoti.libtulip.model.result

sealed class NetworkResult<T> {
    /**
     * This is either a cached value before the online value is finished loading,
     * or the online value itself after it's finished loading
     */
    data class Success<T>(override val data: T) : NetworkResult<T>()

    /**
     * Network fetch failed and there is no cached value available
     */
    data class Error<T>(val error: ErrorType) : NetworkResult<T>() {
        override val data: T? = null
    }

    /**
     * Network fetch failed, but a cached value is available
     */
    data class Cached<T>(override val data: T, val error: ErrorType) : NetworkResult<T>()

    @Suppress("UNUSED")
    enum class ErrorType {
        NO_CONNECTION,
        NOT_FOUND,
        CONVERSION_FAILED
    }

    /**
     * Data of this result or null if not available
     */
    abstract val data: T?
}
