package com.tajmoti.multiplatform.store

import com.tajmoti.libtulip.misc.job.NetworkResult
import kotlinx.coroutines.flow.Flow

interface TStore<Key : Any, Output : Any> {

    fun stream(key: Key): Flow<NetworkResult<Output>>
}