package com.tajmoti.libtulip.model.key

import com.tajmoti.libtulip.model.hosted.StreamingService
import com.tajmoti.libtulip.model.info.TmdbItemId

sealed class TvShowKey : ItemKey {
    data class Hosted(
        val service: StreamingService,
        val tvShowId: String
    ) : TvShowKey(), ItemKey.Hosted

    data class Tmdb(
        val id: TmdbItemId.Tv
    ) : TvShowKey(), ItemKey.Tmdb
}