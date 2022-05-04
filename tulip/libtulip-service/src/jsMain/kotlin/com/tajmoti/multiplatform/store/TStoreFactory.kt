package com.tajmoti.multiplatform.store

import com.tajmoti.libtulip.TulipConfiguration
import com.tajmoti.libtulip.model.result.NetworkResult
import kotlinx.coroutines.flow.*

actual object TStoreFactory {

    actual fun <Key : Any, Output : Any> createStore(
        cache: TulipConfiguration.CacheParameters,
        source: (Key) -> Flow<Result<Output>>,
        reader: ((Key) -> Flow<Output?>)?,
        writer: (suspend (Key, Output) -> Unit)?
    ): TStore<Key, Output> {
        return object : TStore<Key, Output> {
            override fun stream(key: Key, refresh: Boolean): Flow<NetworkResult<Output>> {
                val fromMemory = reader
                    ?.invoke(key)
                    ?.map(::wrapNullable)
                    ?: emptyFlow()
                val fromNetwork = source(key)
                    .map(::toNetworkResult)
                    .onEach { result -> result.data?.let { value -> writer?.invoke(key, value) } }
                return merge(fromNetwork, fromMemory).distinctUntilChanged()
            }

            private fun wrapNullable(maybeOutput: Output?): NetworkResult<Output> {
                return maybeOutput?.let { output -> NetworkResult.Success(output) }
                    ?: NetworkResult.Error(NetworkResult.ErrorType.CONVERSION_FAILED)
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