package com.tajmoti.libtulip.model.key

import com.tajmoti.multiplatform.JvmSerializable

sealed interface StreamableKey : JvmSerializable {
    val itemKey: ItemKey

    sealed interface Hosted : StreamableKey {
        val streamingService: StreamingService
        val id: String
        override val itemKey: ItemKey.Hosted
    }

    sealed interface Tmdb : StreamableKey {
        override val itemKey: ItemKey.Tmdb
    }
}