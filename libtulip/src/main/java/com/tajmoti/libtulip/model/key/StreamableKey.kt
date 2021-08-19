package com.tajmoti.libtulip.model.key

import com.tajmoti.libtulip.model.StreamingService

sealed class StreamableKey {
    abstract val service: StreamingService
}