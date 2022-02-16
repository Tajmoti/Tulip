package com.tajmoti.multiplatform.store

import com.tajmoti.libtulip.TulipConfiguration
import com.tajmoti.libtulip.misc.job.NetworkResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

actual object TStoreFactory {
    actual fun <Key : Any, Output : Any> createStore(
        cache: TulipConfiguration.CacheParameters,
        source: (Key) -> Flow<Result<Output>>,
        reader: ((Key) -> Flow<Output?>)?,
        writer: (suspend (Key, Output) -> Unit)?
    ): TStore<Key, Output> {
        return object : TStore<Key, Output> {
            override fun stream(key: Key): Flow<NetworkResult<Output>> {
                return source(key).map(::toNetworkResult)
            }
        }
    }

    private fun <Output : Any> toNetworkResult(it: Result<Output>): NetworkResult<Output> {
        return it.fold(
            { NetworkResult.Success(it) },
            { NetworkResult.Error(NetworkResult.ErrorType.CONVERSION_FAILED) }
        )
    }
}