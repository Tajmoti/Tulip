package com.tajmoti.libtulip.misc.job

import kotlinx.coroutines.flow.Flow

typealias NetFlow<T> = Flow<NetworkResult<T>>