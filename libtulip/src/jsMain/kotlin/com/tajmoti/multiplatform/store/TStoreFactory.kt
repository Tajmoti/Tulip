package com.tajmoti.multiplatform.store

import com.tajmoti.libtulip.TulipConfiguration
import com.tajmoti.libtulip.misc.job.NetworkResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch

actual object TStoreFactory {
    private val scope = CoroutineScope(Dispatchers.Default)

    actual fun <Key : Any, Output : Any> createStore(
        cache: TulipConfiguration.CacheParameters,
        source: (Key) -> Flow<Result<Output>>,
        reader: ((Key) -> Flow<Output?>)?,
        writer: (suspend (Key, Output) -> Unit)?
    ): TStore<Key, Output> {
        return object : TStore<Key, Output> {
            override fun stream(key: Key): Flow<NetworkResult<Output>> {
                scope.launch { startStreamingToWriter(key) }
                return reader?.invoke(key)?.map(::wrapNullable)
                    ?: source(key).map(::toNetworkResult)
            }

            private fun wrapNullable(maybeOutput: Output?): NetworkResult<Output> {
                return maybeOutput?.let { output -> NetworkResult.Success(output) }
                    ?: NetworkResult.Error(NetworkResult.ErrorType.CONVERSION_FAILED)
            }

            private suspend fun startStreamingToWriter(key: Key) {
                if (writer == null) return
                source(key)
                    .mapNotNull { result -> result.getOrNull() }
                    .collect { output -> writer(key, output) }
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