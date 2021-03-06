package com.tajmoti.multiplatform.store

import com.tajmoti.libtulip.model.result.NetworkResult
import kotlinx.coroutines.flow.Flow

interface TStore<Key : Any, Output : Any> {

    fun stream(key: Key, refresh: Boolean = true): Flow<NetworkResult<Output>>
}