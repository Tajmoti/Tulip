@file:Suppress("NOTHING_TO_INLINE")

package com.tajmoti.commonutils

/**
 * If this result is successful, returns the result produced by [transform],
 * otherwise returns a [Result.failure] converted to the [R] result type.
 */
inline fun <R, T> Result<T>.flatMap(transform: (value: T) -> Result<R>): Result<R> {
    return transform(getOrElse { return Result.failure(exceptionOrNull()!!) })
}

/**
 * Maps the original value along with the new value.
 */
inline fun <R, T> Result<T>.mapZip(transform: (value: T) -> R): Result<Pair<T, R>> {
    return map { it to transform(it) }
}

/**
 * Returns a successful result of all items if all were successful,
 * or failure with the first exception if one of them failed.
 */
inline fun <T : Any> List<Result<T>>.allOrNone(): Result<List<T>> {
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
