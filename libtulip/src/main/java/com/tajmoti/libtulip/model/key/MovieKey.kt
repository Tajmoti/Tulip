package com.tajmoti.libtulip.model.key

import com.tajmoti.libtulip.model.hosted.StreamingService
import com.tajmoti.libtulip.model.info.TmdbItemId

sealed class MovieKey : StreamableKey, ItemKey {
    data class Hosted(
        val service: StreamingService,
        val movieId: String
    ) : MovieKey(), StreamableKey.Hosted, ItemKey.Hosted {
        override val streamingService = service
        override val streamableKey = movieId
    }

    data class Tmdb(
        val id: TmdbItemId.Movie
    ) : MovieKey(), StreamableKey.Tmdb, ItemKey.Tmdb
}
