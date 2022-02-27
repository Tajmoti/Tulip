package com.tajmoti.commonutils

inline fun <T> jsObject(init: T.() -> Unit): T {
    @Suppress("UNCHECKED_CAST")
    val o = object {} as T
    init(o)
    return o
}