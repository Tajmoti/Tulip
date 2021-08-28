package com.tajmoti.libtulip.model.key

import com.tajmoti.libtulip.model.hosted.StreamingService
import com.tajmoti.libtulip.model.tmdb.TmdbItemId

sealed class TvShowKey : ItemKey {
    data class Hosted(
        override val streamingService: StreamingService,
        val tvShowId: String
    ) : TvShowKey(), ItemKey.Hosted {
        override val key: String
            get() = tvShowId
    }

    data class Tmdb(
        override val id: TmdbItemId.Tv
    ) : TvShowKey(), ItemKey.Tmdb
}