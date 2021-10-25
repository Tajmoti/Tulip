package com.tajmoti.libtulip.misc.cache

import arrow.core.Option
import arrow.core.none
import io.ktor.util.date.*

/**
 * FIFO cache, where items also expire by time.
 */
class TimedCache<K, V>(
    /**
     * After how long an item should expire.
     */
    private val timeout: Long,
    /**
     * Maximum allowed cache size.
     */
    private val size: Int
) : Cache<K, V> {
    private val map = hashMapOf<K, Pair<V, Long>>()


    @Synchronized
    override fun set(k: K, v: V) {
        clean()
        map[k] = v to getTimeMillis()
    }

    @Synchronized
    override fun get(k: K): Option<V> {
        clean()
        val pair = map[k] ?: return none()
        val (value, time) = pair
        return Option(value).filter { isStillFresh(time) }
    }

    private fun isStillFresh(time: Long): Boolean {
        return (getTimeMillis() - time) <= timeout
    }

    private fun clean() {
        map.filterNot { isStillFresh(it.value.second) }
            .forEach { map.remove(it.key) }
        val keysToKeep = map.entries.sortedByDescending { it.value.second }
            .take(size)
        val invalidKeys = map.entries - keysToKeep
        invalidKeys.forEach { map.remove(it.key) }
    }
}