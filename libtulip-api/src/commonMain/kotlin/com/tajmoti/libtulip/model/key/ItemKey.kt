package com.tajmoti.libtulip.model.key

import com.tajmoti.libtulip.model.hosted.StreamingService
import com.tajmoti.multiplatform.JvmSerializable

sealed interface ItemKey : JvmSerializable {
    sealed interface Hosted : ItemKey {
        val streamingService: StreamingService
        val id: String
    }

    sealed interface Tmdb : ItemKey {
        val id: Long
    }
}