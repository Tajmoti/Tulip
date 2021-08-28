package com.tajmoti.libtulip.misc

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.last

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
     * Converts throwables from the network into domain errors
     */
    crossinline errorConverter: (Throwable) -> NetworkResult.ErrorType = {
        exceptionToNetworkError(it)
    }
): Flow<NetworkResult<T>> {
    return flow {
        val dbItem = dbProducer()?.also { dbResult ->
            emit(NetworkResult.Success<T>(dbResult))
        }
        networkProducer()
            .onSuccess { netResult ->
                emit(NetworkResult.Success(netResult))
                dbInserter(netResult)
            }
            .onFailure { netErr ->
                val error = errorConverter(netErr)
                if (dbItem != null) {
                    emit(NetworkResult.Cached<T>(dbItem, error))
                } else {
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
suspend inline fun <T> Flow<NetworkResult<T>>.finalValueOrNull(): T? {
    return last().data
}