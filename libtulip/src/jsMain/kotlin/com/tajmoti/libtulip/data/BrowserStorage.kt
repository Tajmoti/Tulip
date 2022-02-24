package com.tajmoti.libtulip.data

import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    private val scope = CoroutineScope(Dispatchers.Default)


    fun put(key: K, value: V?) {
        val keyString = makeKeyString(key)
        if (value != null) {
            localStorage.setItem(keyString, serializer(value))
        } else {
            localStorage.removeItem(keyString)
        }
        window.dispatchEvent(Event(makeKeyString(key)))
    }

    fun get(key: K): Flow<V?> {
        return channelFlow {
            send(forceGet(key))
            val eventKey = makeKeyString(key)
            val listener: (Event) -> Unit = { scope.launch { send(forceGet(key)) } }
            window.addEventListener(eventKey, listener)
            @OptIn(ExperimentalCoroutinesApi::class)
            invokeOnClose { window.removeEventListener(eventKey, listener) }
        }.distinctUntilChanged()
    }

    fun update(key: K, updater: (V?) -> V?) {
        put(key, updater(forceGet(key)))
    }

    private fun forceGet(key: K): V? {
        return localStorage.getItem(makeKeyString(key))
            ?.let { deserializer(it) }
    }

    private fun makeKeyString(key: K): String {
        return prefix + '_' + key.toString()
    }
}

@Suppress("FunctionName")
inline fun <K, reified V : Any> BrowserStorage(prefix: String): BrowserStorage<K, V> {
    return BrowserStorage(prefix, { Json.encodeToString(it) }, { Json.decodeFromString(it) })
}