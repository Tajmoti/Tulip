package com.tajmoti.libtulip.misc.job

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last

sealed class NetworkResult<T> {
    /**
     * This is either a cached value before the online value is finished loading,
     * or the online value itself after it's finished loading
     */
    class Success<T>(override val data: T) : NetworkResult<T>()

    /**
     * Network fetch failed and there is no cached value available
     */
    class Error<T>(val error: ErrorType) : NetworkResult<T>() {
        override val data: T? = null
    }

    /**
     * Network fetch failed, but a cached value is available
     */
    class Cached<T>(override val data: T, val error: ErrorType) : NetworkResult<T>()

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

    /**
     * Turn this network result into a Kotlin stdlib Result type.
     */
    fun toResult(): Result<T> {
        return when (this) {
            is Error<T> -> Result.failure<T>(Throwable("Unsuccessful NetworkResult $error"))
            else -> Result.success(data!!)
        }
    }

    fun <S> map(func: (T) -> S): NetworkResult<S> {
        return when (this) {
            is Success<T> -> Success(func(data))
            is Error<T> -> Error(error)
            is Cached<T> -> Cached(func(data), error)
        }
    }

    fun <S> convert(func: (T) -> S?): NetworkResult<S> {
        return when (this) {
            is Success<T> -> {
                val converted = func(data)
                if (converted != null) {
                    Success(converted)
                } else {
                    Error(ErrorType.CONVERSION_FAILED)
                }
            }
            is Error<T> -> Error(error)
            is Cached<T> -> {
                val converted = func(data)
                if (converted != null) {
                    Cached(converted, error)
                } else {
                    Error(ErrorType.CONVERSION_FAILED)
                }
            }
        }
    }
}

/**
 * Returns the final value of the resource or null if it's not available.
 */
suspend inline fun <T> Flow<NetworkResult<out T>>.firstValueOrNull(): T? {
    return first().data
}

/**
 * Returns the final value of the resource or null if it's not available.
 */
suspend inline fun <T> Flow<NetworkResult<out T>>.finalValueOrNull(): T? {
    return last().data
}