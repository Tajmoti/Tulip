package com.tajmoti.libtulip.model.key

import com.tajmoti.libtulip.model.hosted.StreamingService
import kotlinx.serialization.Serializable

sealed interface TvShowKey : ItemKey {
    @Serializable
    data class Hosted(
        override val streamingService: StreamingService,
        override val id: String
    ) : TvShowKey, ItemKey.Hosted

    @Serializable
    data class Tmdb(
        override val id: Long
    ) : TvShowKey, ItemKey.Tmdb
}