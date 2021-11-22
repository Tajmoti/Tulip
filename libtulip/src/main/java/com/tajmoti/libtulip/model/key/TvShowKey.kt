package com.tajmoti.libtulip.model.key

import com.tajmoti.libtulip.model.hosted.StreamingService

sealed interface TvShowKey : ItemKey {
    data class Hosted(
        override val streamingService: StreamingService,
        override val id: String
    ) : TvShowKey, ItemKey.Hosted

    data class Tmdb(
        override val id: Long
    ) : TvShowKey, ItemKey.Tmdb
}