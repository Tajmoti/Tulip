package com.tajmoti.libtulip.model.result

import kotlinx.coroutines.flow.Flow

typealias NetFlow<T> = Flow<NetworkResult<T>>