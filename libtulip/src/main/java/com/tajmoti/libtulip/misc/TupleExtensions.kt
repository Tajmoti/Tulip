@file:Suppress("UNUSED", "NOTHING_TO_INLINE")

package com.tajmoti.libtulip.misc

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