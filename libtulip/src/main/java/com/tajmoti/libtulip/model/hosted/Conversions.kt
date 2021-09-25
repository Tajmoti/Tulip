@file:Suppress("NOTHING_TO_INLINE")

package com.tajmoti.libtulip.model.hosted

import com.tajmoti.libtulip.model.key.ItemKey
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.model.tmdb.TmdbItemId

inline fun TmdbItemId.toItemKey(): ItemKey.Tmdb {
    return when (this) {
        is TmdbItemId.Tv -> TvShowKey.Tmdb(this)
        is TmdbItemId.Movie -> MovieKey.Tmdb(this)
    }
}

inline fun TmdbItemId.toTvKey(): TvShowKey.Tmdb {
    return TvShowKey.Tmdb(TmdbItemId.Tv(id))
}

inline fun TmdbItemId.toMovieKey(): MovieKey.Tmdb {
    return MovieKey.Tmdb(TmdbItemId.Movie(id))
}
