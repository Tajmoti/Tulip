package com.tajmoti.libtulip.model.key

import com.tajmoti.libtulip.model.hosted.StreamingService
import com.tajmoti.libtulip.model.tmdb.TmdbItemId

sealed interface MovieKey : StreamableKey, ItemKey {
    data class Hosted(
        override val streamingService: StreamingService,
        override val id: String
    ) : MovieKey, StreamableKey.Hosted, ItemKey.Hosted

    data class Tmdb(
        override val id: TmdbItemId.Movie
    ) : MovieKey, StreamableKey.Tmdb, ItemKey.Tmdb
}
