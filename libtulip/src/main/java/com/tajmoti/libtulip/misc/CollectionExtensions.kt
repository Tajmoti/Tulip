@file:Suppress("UNUSED", "NOTHING_TO_INLINE")

package com.tajmoti.libtulip.misc

inline fun <T> Collection<T?>.takeIfNoneNull(): List<T>? {
    val notNull = filterNotNull()
    return if (notNull.size == this.size) {
        notNull
    } else {
        null
    }
}

inline fun <T> Collection<T?>.takeNotNullIfAny(): List<T>? {
    return filterNotNull().ifEmpty<List<T>, List<T>?> { null }
}
