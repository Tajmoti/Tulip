package com.tajmoti.multiplatform.store

import com.tajmoti.libtulip.TulipConfiguration
import kotlinx.coroutines.flow.Flow

expect object TStoreFactory {
    fun <Key : Any, Output : Any> createStore(
        cache: TulipConfiguration.CacheParameters,
        source: (Key) -> Flow<Result<Output>>,
        reader: ((Key) -> Flow<Output?>)? = null,
        writer: (suspend (Key, Output) -> Unit)? = null,
    ): TStore<Key, Output>
}