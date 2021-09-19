package com.tajmoti.libtulip.misc

import com.tajmoti.commonutils.logger
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
     * Retrieves the item from the cache
     */
    crossinline cacheGetter: () -> T?,
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
        val cached = cacheGetter()?.also {
            emit(NetworkResult.Success<T>(it))
            if (finishIfCached)
                return@flow
        }
        val dbItem = dbProducer()?.also { dbResult ->
            emit(NetworkResult.Success<T>(dbResult))
        }
        networkProducer()
            .onSuccess { netResult ->
                emit(NetworkResult.Success(netResult))
                dbInserter(netResult)
                cachePutter(netResult)
            }
            .onFailure { netErr ->
                val error = errorConverter(netErr)
                when {
                    cached != null -> emit(NetworkResult.Cached<T>(cached, error))
                    dbItem != null -> emit(NetworkResult.Cached<T>(dbItem, error))
                    else -> emit(NetworkResult.Error<T>(error))
                }
            }
    }
}

/**
 * Retrieves a resource, either from a local database or from the network.
 * First, a database item is used and when the network request finished,
 * the online value is used and inserted into the database.
 * On online fetching failure, DB item is used as fallback.
 *
 * The difference from the first variant is that here the fetched/inserted item
 * may be different from the item that comes from the DB.
 */
inline fun <T, S> getNetworkBoundResourceVariable(
    /**
     * Retrieves the value from local DB or null if it's not present
     */
    crossinline dbProducer: suspend () -> T?,
    /**
     * Fetches the online value
     */
    crossinline networkProducer: suspend () -> Result<S>,
    /**
     * Updates the database with the online value
     */
    crossinline dbInserter: suspend (S) -> Unit,
    /**
     * Converts the fetched item into the form that can be returned.
     * This operation can fail and return null if the item isn't good.
     */
    crossinline fromNetworkConverter: suspend (S) -> T?,
    /**
     * Retrieves the item from the cache
     */
    crossinline cacheGetter: () -> S?,
    /**
     * Updates the cache
     */
    crossinline cachePutter: (S) -> Unit,
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
): Flow<NetworkResult<out T>> {
    return flow {
        // First try to emit a cached value
        val cached = cacheGetter()?.let { cached ->
            // Cached value available, try to convert it
            fromNetworkConverter(cached).onValue { converted ->
                // Conversion successful, use it
                logger.debug("Emitting cached value #1")
                emit(NetworkResult.Success<T>(converted))
                // If we are fine with cached-only, get out of here
                if (finishIfCached)
                    return@flow
            }
        }
        // Then try to emit an item from the DB
        val dbItem = dbProducer().onValue { dbResult ->
            // DB item available, use it
            logger.debug("Emitting DB value #1")
            emit(NetworkResult.Success<T>(dbResult))
        }
        // Then try to fetch it from network
        networkProducer()
            .onSuccess { netResult ->
                // Network succeeded, save it, cache it and try to convert it
                dbInserter(netResult)
                cachePutter(netResult)
                fromNetworkConverter(netResult)
                    .onValue { converted ->
                        // Conversion successful, use it
                        logger.debug("Emitting network value #1")
                        emit(NetworkResult.Success(converted))
                    }
                    .onNull {
                        // Conversion failed, try to use a cached value
                        cached.onValue { cached ->
                            // Cached value available, use it and get out of here
                            logger.debug("Emitting cached value #2")
                            emit(
                                NetworkResult.Cached<T>(
                                    cached, NetworkResult.ErrorType.CONVERSION_FAILED
                                )
                            )
                        }.onNull {
                            // Cached value not available, try DB value
                            dbItem.onValue { dbItem ->
                                // DB item available, use it and get out of here
                                logger.debug("Emitting DB value #2")
                                emit(
                                    NetworkResult.Cached<T>(
                                        dbItem, NetworkResult.ErrorType.CONVERSION_FAILED
                                    )
                                )
                            }.onNull {
                                // DB item available, return conversion error
                                logger.debug("Emitting error value #1")
                                emit(
                                    NetworkResult.Error<T>(
                                        NetworkResult.ErrorType.CONVERSION_FAILED
                                    )
                                )
                            }
                        }
                    }
            }
            .onFailure { netErr ->
                val error = errorConverter(netErr)
                // If we have a cached value, return it
                cached.onValue { cached ->
                    logger.debug("Emitting cached value #3")
                    emit(NetworkResult.Cached<T>(cached, error))
                    return@flow
                }
                // Cached value not available, try DB value
                dbItem.onValue { dbItem ->
                    // DB item available, use it and get out of here
                    logger.debug("Emitting DB value #3")
                    emit(NetworkResult.Cached<T>(dbItem, error))
                }.onNull {
                    // DB item available, return conversion error
                    logger.debug("Emitting error value #2")
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