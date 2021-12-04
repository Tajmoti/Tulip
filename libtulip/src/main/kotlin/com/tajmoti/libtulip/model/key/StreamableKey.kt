package com.tajmoti.libtulip.model.key

import com.tajmoti.libtulip.model.hosted.StreamingService
import java.io.Serializable

sealed interface StreamableKey : Serializable {
    sealed interface Hosted : StreamableKey {
        val streamingService: StreamingService
        val id: String
    }

    sealed interface Tmdb : StreamableKey
}