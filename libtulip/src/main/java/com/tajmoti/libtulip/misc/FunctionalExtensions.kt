@file:Suppress("UNUSED", "NOTHING_TO_INLINE")
package com.tajmoti.libtulip.misc

inline fun <T> T?.onValue(onValue: (T) -> Unit): T? {
    if (this != null) {
        onValue(this)
    }
    return this
}

inline fun <T> T?.onNull(onError: () -> Unit): T? {
    if (this == null) {
        onError()
    }
    return this
}

inline fun <T> T?.fold(onValue: (T) -> Unit, onError: () -> Unit) {
    if (this == null) {
        onError()
    } else {
        onValue(this)
    }
}