package com.tajmoti.libtulip.misc

interface Cache<K, V> {

    operator fun set(k: K, v: V)

    operator fun get(k: K): V?
}