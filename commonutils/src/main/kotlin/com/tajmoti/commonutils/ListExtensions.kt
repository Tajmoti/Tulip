@file:Suppress("NOTHING_TO_INLINE")

package com.tajmoti.commonutils

/**
 * Flattens the iterable selected by [selector] and applies the [transform] function
 * to produce the final value to place in the resulting list.
 */
inline fun <T, R, S> Iterable<T>.flatMapWithTransform(
    selector: (T) -> Iterable<R>,
    transform: (T, R) -> S
): List<S> {
    val destination = arrayListOf<S>()
    for (element in this) {
        val list = selector(element)
        destination.addAll(list.map { transform(element, it) })
    }
    return destination
}