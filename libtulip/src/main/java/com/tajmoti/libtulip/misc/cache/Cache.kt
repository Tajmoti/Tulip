package com.tajmoti.libtulip.misc.cache

import arrow.core.Option

interface Cache<K, V> {

    operator fun set(k: K, v: V)

    operator fun get(k: K): Option<V>
}