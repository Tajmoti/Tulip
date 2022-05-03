package com.tajmoti.multiplatform.store

import com.tajmoti.libtulip.TulipConfiguration
import kotlinx.coroutines.flow.Flow

actual object TStoreFactory {
    actual fun <Key : Any, Output : Any> createStore(
        cache: TulipConfiguration.CacheParameters,
        source: (Key) -> Flow<Result<Output>>,
        reader: ((Key) -> Flow<Output?>)?,
        writer: (suspend (Key, Output) -> Unit)?,
    ): TStore<Key, Output> {
        return JvmTStore(cache, source, reader, writer)
    }
}