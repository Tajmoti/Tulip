package com.tajmoti.libtulip.model.key

import com.tajmoti.libtulip.model.hosted.StreamingService
import java.io.Serializable

sealed interface ItemKey : Serializable {
    sealed interface Hosted : ItemKey {
        val streamingService: StreamingService
        val id: String
    }

    sealed interface Tmdb : ItemKey {
        val id: Long
    }
}