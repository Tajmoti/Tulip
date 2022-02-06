package com.tajmoti.libtulip.model.key

import com.tajmoti.multiplatform.JvmSerializable
import com.tajmoti.libtulip.model.hosted.StreamingService

sealed interface StreamableKey : JvmSerializable {
    sealed interface Hosted : StreamableKey {
        val streamingService: StreamingService
        val id: String
    }

    sealed interface Tmdb : StreamableKey
}