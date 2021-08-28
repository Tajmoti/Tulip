package com.tajmoti.libtulip.model.key

import com.tajmoti.libtulip.model.hosted.StreamingService
import com.tajmoti.libtulip.model.tmdb.TmdbItemId

sealed class MovieKey : StreamableKey, ItemKey {
    data class Hosted(
        val service: StreamingService,
        val movieId: String
    ) : MovieKey(), StreamableKey.Hosted, ItemKey.Hosted {
        override val streamingService = service
        override val streamableKey = movieId
        override val key = movieId
    }

    data class Tmdb(
        override val id: TmdbItemId.Movie
    ) : MovieKey(), StreamableKey.Tmdb, ItemKey.Tmdb
}
