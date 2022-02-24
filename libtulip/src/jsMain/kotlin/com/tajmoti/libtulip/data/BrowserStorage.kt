package com.tajmoti.libtulip.data

import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.w3c.dom.StorageEvent

class BrowserStorage<K, V : Any>(
    val serializer: (V) -> String,
    val deserializer: (String) -> V
) {
    private val scope = CoroutineScope(Dispatchers.Default)


    fun put(key: K, value: V) {
        window.localStorage.setItem(key.toString(), serializer(value))
    }

    fun get(key: K): Flow<V?> {
        return channelFlow {
            send(forceGet(key))
            window.addEventListener("storage", {
                val se = it as StorageEvent
                if (se.key == key && it.newValue != null) {
                    scope.launch { send(deserializer(it.newValue!!)) }
                }
            })
        }.distinctUntilChanged()
    }

    fun update(key: K, updater: (V?) -> V) {
        put(key, updater(forceGet(key)))
    }

    private fun forceGet(key: K): V? {
        return window.localStorage.getItem(key.toString())
            ?.let { deserializer(it) }
    }
}

@Suppress("FunctionName")
inline fun <K, reified V : Any> BrowserStorage(): BrowserStorage<K, V> {
    return BrowserStorage({ Json.encodeToString(it) }, { Json.decodeFromString(it) })
}