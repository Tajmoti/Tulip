package com.tajmoti.libtulip.misc.job

import com.dropbox.android.external.store4.FetcherResult
import com.dropbox.android.external.store4.MemoryPolicy
import com.dropbox.android.external.store4.StoreResponse
import com.tajmoti.libtulip.TulipConfiguration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlin.time.Duration
import kotlin.time.ExperimentalTime


fun <T : Any> Result<T>.toFetcherResult(): FetcherResult<T> {
    return fold({ FetcherResult.Data(it) }, { FetcherResult.Error.Exception(it) })
}

@OptIn(ExperimentalTime::class)
fun <K : Any, V : Any> createCache(params: TulipConfiguration.CacheParameters): MemoryPolicy<K, V> {
    return MemoryPolicy.builder<K, V>()
        .setMaxSize(params.size.toLong())
        .setExpireAfterWrite(Duration.milliseconds(params.validityMs))
        .build()
}

fun <T : Any> Flow<StoreResponse<T>>.toNetFlow(): Flow<NetworkResult<T>> {
    return this.mapNotNull { it.toNetworkResult() }
}

fun <T : Any> StoreResponse<T>.toNetworkResult(): NetworkResult<T>? {
    return when (this) {
        is StoreResponse.Data -> NetworkResult.Success(value)
        is StoreResponse.Loading -> null
        is StoreResponse.NoNewData -> NetworkResult.Error(NetworkResult.ErrorType.NOT_FOUND)
        is StoreResponse.Error -> NetworkResult.Error(NetworkResult.ErrorType.NO_CONNECTION)
    }
}