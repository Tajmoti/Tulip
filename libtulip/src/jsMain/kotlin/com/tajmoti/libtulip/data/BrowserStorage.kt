package com.tajmoti.libtulip.data

import kotlinx.browser.window
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.w3c.dom.events.Event

/**
 * Saves instances of [V] under [K] into the LocalStorage of the browser.
 */
class BrowserStorage<K, V : Any>(
    /**
     * Unique prefix differentiating this storage object
     * from other one which might be using the same key.
     * Must be different for all instances of this class,
     * however should be the same across reloads of the app.
     */
    private val prefix: String,
    /**
     * Serializes the [V] instances to save to LocalStorage.
     */
    private val serializer: (V) -> String,
    /**
     * Deserializes [V] instances from strings produced by [serializer].
     */
    private val deserializer: (String) -> V
) {
    private val localStorage = window.localStorage


    fun put(key: K, value: V?) {
        val keyString = makeKeyString(key)
        if (value != null) {
            localStorage.setItem(keyString, serializer(value))
        } else {
            localStorage.removeItem(keyString)
        }
        notifyAdded(key)
    }

    fun get(key: K): Flow<V?> {
        val eventKey = makeKeyString(key)
        val getter: () -> V? = { forceGet(key) }
        return getByEvent(getter, eventKey)
    }

    fun getAll(): Flow<List<V>> {
        val eventKey = prefix
        val getter: () -> List<V> = { forceGetAll() }
        return getByEvent(getter, eventKey)
    }

    private fun notifyAdded(key: K) {
        window.dispatchEvent(Event(makeKeyString(key)))
        window.dispatchEvent(Event(prefix))
    }

    private fun <T> getByEvent(getter: () -> T, eventKey: String): Flow<T> {
        return channelFlow {
            send(getter())
            val listener: (Event) -> Unit = { launch { send(getter()) } }
            window.addEventListener(eventKey, listener)
            awaitClose { window.removeEventListener(eventKey, listener) }
        }.distinctUntilChanged()
    }

    fun update(key: K, updater: (V?) -> V?) {
        put(key, updater(forceGet(key)))
    }

    private fun forceGet(key: K): V? {
        val keyString = makeKeyString(key)
        return forceGetByKeyString(keyString)
    }

    private fun forceGetAll(): List<V> {
        return getAllKeys()
            .mapNotNull { key -> forceGetByKeyString(key) }
    }

    private fun forceGetByKeyString(keyString: String): V? {
        return localStorage.getItem(keyString)
            ?.let { deserializer(it) }
    }

    private fun getAllKeys(): Set<String> {
        return (0 until localStorage.length)
            .mapNotNull { index -> localStorage.key(index) }
            .filter { key -> key.startsWith(prefix) }
            .toSet()
    }

    private fun makeKeyString(key: K): String {
        return prefix + '_' + key.toString()
    }
}

@Suppress("FunctionName")
inline fun <K, reified V : Any> BrowserStorage(prefix: String): BrowserStorage<K, V> {
    return BrowserStorage(prefix, { Json.encodeToString(it) }, { Json.decodeFromString(it) })
}

@Suppress("FunctionName")
inline fun <K, reified V : Any> Any.BrowserStorage(index: Int = 0): BrowserStorage<K, V> {
    return BrowserStorage("${this::class.simpleName!!}_${V::class.simpleName}_${index}")
}