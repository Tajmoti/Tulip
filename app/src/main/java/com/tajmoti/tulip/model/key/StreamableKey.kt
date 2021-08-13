package com.tajmoti.tulip.model.key

import com.tajmoti.tulip.model.StreamingService

sealed class StreamableKey {
    abstract val service: StreamingService
}