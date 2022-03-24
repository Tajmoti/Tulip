package com.tajmoti.multiplatform.store

import com.dropbox.android.external.store4.*
import com.tajmoti.libtulip.TulipConfiguration
import com.tajmoti.libtulip.misc.job.NetworkResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class JvmTStore<Key : Any, Output : Any>(
    cache: TulipConfiguration.CacheParameters,
    source: (Key) -> Flow<Result<Output>>,
    reader: ((Key) -> Flow<Output?>)?,
    writer: (suspend (Key, Output) -> Unit)?,
) : TStore<Key, Output> {
    private val impl = makeBuilder(source, reader, writer)
        .cachePolicy(createCache(cache))
        .build()

    override fun stream(key: Key, refresh: Boolean): Flow<NetworkResult<Output>> {
        return impl.stream(StoreRequest.cached(key, refresh))
            .distinctUntilChanged()
            .toNetFlow()
    }

    private fun <K : Any, V : Any> createCache(params: TulipConfiguration.CacheParameters): MemoryPolicy<K, V> {
        return MemoryPolicy.builder<K, V>()
            .setMaxSize(params.size.toLong())
            .setExpireAfterWrite(params.validityMs.milliseconds)
            .build()
    }

    private fun <T : Any> Flow<StoreResponse<T>>.toNetFlow(): Flow<NetworkResult<T>> {
        return this.mapNotNull { it.toNetworkResult() }
    }

    private fun <T : Any> StoreResponse<T>.toNetworkResult(): NetworkResult<T>? {
        return when (this) {
            is StoreResponse.Data -> NetworkResult.Success(value)
            is StoreResponse.Loading -> null
            is StoreResponse.NoNewData -> NetworkResult.Error(NetworkResult.ErrorType.NOT_FOUND)
            is StoreResponse.Error -> NetworkResult.Error(NetworkResult.ErrorType.NO_CONNECTION)
        }
    }

    companion object {
        private fun <T : Any> Result<T>.toFetcherResult(): FetcherResult<T> {
            return fold({ FetcherResult.Data(it) }, { FetcherResult.Error.Exception(it) })
        }

        private fun <Key : Any, Output : Any> makeBuilder(
            source: (Key) -> Flow<Result<Output>>,
            reader: ((Key) -> Flow<Output?>)?,
            writer: (suspend (Key, Output) -> Unit)?
        ): StoreBuilder<Key, Output> {
            val x = makeSource(reader, writer)
            val fetcher = Fetcher.ofResultFlow<Key, Output> { key -> source(key).map { it.toFetcherResult() } }
            return if (x != null) {
                StoreBuilder.from(fetcher, x)
            } else {
                StoreBuilder.from(fetcher)
            }
        }

        private fun <Key : Any, Output : Any> makeSource(
            reader: ((Key) -> Flow<Output?>)?,
            writer: (suspend (Key, Output) -> Unit)?,
        ): SourceOfTruth<Key, Output, Output>? {
            if (reader == null) return null
            return SourceOfTruth.of(
                reader,
                { k, v -> writer?.invoke(k, v) },
            )
        }
    }
}