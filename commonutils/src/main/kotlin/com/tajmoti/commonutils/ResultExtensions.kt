@file:Suppress("NOTHING_TO_INLINE")
package com.tajmoti.commonutils

import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/**
 * If this result is successful, returns the result produced by [transform],
 * otherwise returns a [Result.failure] converted to the [R] result type.
 */
inline fun <R, T> Result<T>.flatMap(transform: (value: T) -> Result<R>): Result<R> {
    return when {
        isSuccess -> transform(getOrNull()!!)
        else -> Result.failure(exceptionOrNull()!!)
    }
}

/**
 * If this result is successful, returns the original value paired result produced by [transform],
 * otherwise returns a [Result.failure] converted to the [R] result type.
 */
inline fun <R, T> Result<T>.flatMapZip(transform: (value: T) -> Result<R>): Result<Pair<T, R>> {
    return when {
        isSuccess -> {
            val value = getOrNull()!!
            transform(value).pairWithReverse(value)
        }
        else -> Result.failure(exceptionOrNull()!!)
    }
}

/**
 * If this result is successful, returns the result produced by [transform],
 * otherwise returns a [Result.failure] converted to the [R] result type.
 *
 * The transform is executed with the provided [context].
 */
suspend inline fun <R, T> Result<T>.flatMapWithContext(
    context: CoroutineContext,
    crossinline transform: suspend (value: T) -> Result<R>
): Result<R> {
    map { }
    return when {
        isSuccess -> withContext(context) { transform(getOrNull()!!) }
        else -> Result.failure(exceptionOrNull()!!)
    }
}

/**
 * Just like [mapWithContext], except that the transform is executed with the provided [context].
 */
suspend inline fun <R, T> Result<T>.mapWithContext(
    context: CoroutineContext,
    crossinline transform: (value: T) -> R
): Result<R> {
    return when {
        isSuccess -> withContext(context) { Result.success(transform(getOrNull()!! as T)) }
        else -> Result.failure(exceptionOrNull()!!)
    }
}

/**
 * Maps the original value along with the new value.
 */
inline fun <R, T> Result<T>.mapZip(transform: (value: T) -> R): Result<Pair<T, R>> {
    return map { it to transform(it) }
}

/**
 * Maps the original value along with the new value.
 */
inline fun <R, T> Result<T>.pairWith(other: R): Result<Pair<T, R>> {
    return map { it to other }
}

/**
 * Maps the original value along with the new value.
 */
inline fun <R, T> Result<T>.pairWithReverse(other: R): Result<Pair<R, T>> {
    return map { other to it }
}

/**
 * Returns a successful result of all items if all were successful,
 * or failure with the first exception if one of them failed.
 */
inline fun <T> List<Result<T>>.allOrNone(): Result<List<T>> {
    if (isEmpty()) {
        return Result.success(emptyList())
    }
    val ok = filter { it.isSuccess }
    return if (ok.size == size) {
        Result.success(map { it.getOrNull()!! })
    } else {
        val exception = first { it.exceptionOrNull() != null }.exceptionOrNull()!!
        Result.failure(exception)
    }
}
