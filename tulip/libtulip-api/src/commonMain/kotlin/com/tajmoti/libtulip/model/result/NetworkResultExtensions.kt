package com.tajmoti.libtulip.model.result

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.map


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


/**
 * Turn this network result into a Kotlin stdlib Result type.
 */
fun <T> NetworkResult<T>.toResult(): Result<T> {
    return when (this) {
        is NetworkResult.Error<T> -> Result.failure(Throwable("Unsuccessful NetworkResult $error"))
        is NetworkResult.Success<T> -> Result.success(data)
        is NetworkResult.Cached -> Result.success(data)
    }
}

fun <S, T> NetworkResult<T>.map(func: (T) -> S): NetworkResult<S> {
    return when (this) {
        is NetworkResult.Success<T> -> NetworkResult.Success(func(data))
        is NetworkResult.Error<T> -> NetworkResult.Error(error)
        is NetworkResult.Cached<T> -> NetworkResult.Cached(func(data), error)
    }
}

fun <S, T> Flow<NetworkResult<out T>>.mapEach(transform: (T) -> S): Flow<NetworkResult<S>> {
    return map { result -> result.map { item -> transform(item) } }
}

fun <S, T> Flow<NetworkResult<out List<T>>>.mapEachInList(transform: (T) -> S): Flow<NetworkResult<List<S>>> {
    return map { result -> result.map { items -> items.map(transform) } }
}

fun <A, B, C> combineResults(a: NetworkResult<A>, b: NetworkResult<B>, transform: (A, B) -> C): NetworkResult<C> {
    val aData = a.data ?: return NetworkResult.Error(NetworkResult.ErrorType.CONVERSION_FAILED)
    val bData = b.data ?: return NetworkResult.Error(NetworkResult.ErrorType.CONVERSION_FAILED)
    return NetworkResult.Success(transform(aData, bData))
}

fun <A, B, C, D> combineResults(a: NetworkResult<A>, b: NetworkResult<B>, c: NetworkResult<C>, transform: (A, B, C) -> D): NetworkResult<D> {
    val aData = a.data ?: return NetworkResult.Error(NetworkResult.ErrorType.CONVERSION_FAILED)
    val bData = b.data ?: return NetworkResult.Error(NetworkResult.ErrorType.CONVERSION_FAILED)
    val cData = c.data ?: return NetworkResult.Error(NetworkResult.ErrorType.CONVERSION_FAILED)
    return NetworkResult.Success(transform(aData, bData, cData))
}

fun <S, T> NetworkResult<T>.convert(func: (T) -> S?): NetworkResult<S> {
    return when (this) {
        is NetworkResult.Success<T> -> {
            val converted = func(data)
            if (converted != null) {
                NetworkResult.Success(converted)
            } else {
                NetworkResult.Error(NetworkResult.ErrorType.CONVERSION_FAILED)
            }
        }
        is NetworkResult.Error<T> -> NetworkResult.Error(error)
        is NetworkResult.Cached<T> -> {
            val converted = func(data)
            if (converted != null) {
                NetworkResult.Cached(converted, error)
            } else {
                NetworkResult.Error(NetworkResult.ErrorType.CONVERSION_FAILED)
            }
        }
    }
}