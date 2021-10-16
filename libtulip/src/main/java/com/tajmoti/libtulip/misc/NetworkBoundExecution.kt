package com.tajmoti.libtulip.misc

import arrow.core.Option
import arrow.core.Some
import com.tajmoti.commonutils.logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.last

/**
 * Set to true to enable debugging of the network resource mechanism.
 */
private const val NETWORK_EXECUTION_DEBUG = false

fun Any.networkBoundResourceDebug(message: String) {
    if (NETWORK_EXECUTION_DEBUG)
        logger.debug(message)
}

/**
 * Retrieves a resource, either from a local database or from the network.
 * First, a database item is used and when the network request finished,
 * the online value is used and inserted into the database.
 * On online fetching failure, DB item is used as fallback.
 */
inline fun <T> getNetworkBoundResource(
    /**
     * Retrieves the value from local DB or null if it's not present
     */
    crossinline dbProducer: suspend () -> T?,
    /**
     * Fetches the online value
     */
    crossinline networkProducer: suspend () -> Result<T>,
    /**
     * Updates the database with the online value
     */
    crossinline dbInserter: suspend (T) -> Unit,
    /**
     * Retrieves the item from the cache
     */
    crossinline cacheGetter: () -> Option<T>,
    /**
     * Updates the cache
     */
    crossinline cachePutter: (T) -> Unit,
    /**
     * Converts throwables from the network into domain errors
     */
    crossinline errorConverter: (Throwable) -> NetworkResult.ErrorType = {
        exceptionToNetworkError(it)
    },
    /**
     * If true, the resource won't be pulled from the DB or the network if it's already cached.
     */
    finishIfCached: Boolean = true
): Flow<NetworkResult<T>> {
    return flow {
        // First try to emit a cached value
        val cached = cacheGetter()
        if (cached is Some) {
            // Cached value available, try to convert it
            networkBoundResourceDebug("Emitting cached value #1")
            emit(NetworkResult.Success<T>(cached.value))
            // If we are fine with cached-only, get out of here
            if (finishIfCached)
                return@flow
        }
        // Then try to emit an item from the DB
        val dbItem = dbProducer().onValue { dbResult ->
            // DB item available, use it
            networkBoundResourceDebug("Emitting DB value #1")
            emit(NetworkResult.Success<T>(dbResult))
        }
        // Then try to fetch it from network
        networkProducer()
            .onSuccess { netResult ->
                // Network succeeded, save it, cache it and try to convert it
                dbInserter(netResult)
                cachePutter(netResult)
                networkBoundResourceDebug("Emitting network value #1")
                emit(NetworkResult.Success(netResult))
            }
            .onFailure { netErr ->
                val error = errorConverter(netErr)
                // If we have a cached value, return it
                if (cached is Some) {
                    networkBoundResourceDebug("Emitting cached value #3")
                    emit(NetworkResult.Cached<T>(cached.value, error))
                    return@flow
                }
                // Cached value not available, try DB value
                dbItem.onValue { dbItem ->
                    // DB item available, use it and get out of here
                    networkBoundResourceDebug("Emitting DB value #3")
                    emit(NetworkResult.Cached<T>(dbItem, error))
                }.onNull {
                    // DB item available, return conversion error
                    networkBoundResourceDebug("Emitting error value #2")
                    emit(NetworkResult.Error<T>(error))
                }
            }
    }
}

@Suppress("NOTHING_TO_INLINE", "UNUSED_PARAMETER")
inline fun exceptionToNetworkError(exception: Throwable): NetworkResult.ErrorType {
    return NetworkResult.ErrorType.NO_CONNECTION // TODO Proper error handling
}

/**
 * Returns the final value of the resource or null if it's not available.
 */
suspend inline fun <T> Flow<NetworkResult<out T>>.finalValueOrNull(): T? {
    return last().data
}

/**
 * Returns the final value of the resource or null if it's not available.
 */
suspend inline fun <T> Flow<NetworkResult<out T>>.firstValueOrNull(): T? {
    return first().data
}