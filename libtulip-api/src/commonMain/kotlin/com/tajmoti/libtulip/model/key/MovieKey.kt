package com.tajmoti.libtulip.model.key

import com.tajmoti.libtulip.model.hosted.StreamingService
import kotlinx.serialization.Serializable

sealed interface MovieKey : StreamableKey, ItemKey {
    @Serializable
    data class Hosted(
        override val streamingService: StreamingService,
        override val id: String
    ) : MovieKey, StreamableKey.Hosted, ItemKey.Hosted {
        override val itemKey: ItemKey.Hosted
            get() = this
    }

    @Serializable
    data class Tmdb(
        override val id: Long
    ) : MovieKey, StreamableKey.Tmdb, ItemKey.Tmdb {
        override val itemKey: ItemKey.Tmdb
            get() = this
    }
}
