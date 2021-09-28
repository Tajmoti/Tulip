@file:Suppress("UNUSED", "NOTHING_TO_INLINE")

package com.tajmoti.libtulip.misc

inline fun <T> List<T?>.takeIfNoneNull(): List<T>? {
    val notNull = filterNotNull()
    return if (notNull.size == this.size) {
        notNull
    } else {
        null
    }
}

inline fun <T> List<T?>.takeNotNullIfAny(): List<T>? {
    val notNull = filterNotNull()
    return if (notNull.isEmpty()) {
        null
    } else {
        notNull
    }
}

inline fun <A, B> Pair<A?, B?>.takeIfNoneNull(): Pair<A, B>? {
    val (a, b) = this
    a?.let { b?.let { return Pair(a, b) } }
    return null
}

inline fun <A, B, C> Triple<A?, B?, C?>.takeIfNoneNull(): Triple<A, B, C>? {
    val (a, b, c) = this
    a?.let { b?.let { c?.let { return Triple(a, b, c) } } }
    return null
}
