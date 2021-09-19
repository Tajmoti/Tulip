@file:Suppress("NOTHING_TO_INLINE")

package com.tajmoti.libtulip.model.hosted

import com.tajmoti.libtmdb.model.tv.SlimSeason
import com.tajmoti.libtmdb.model.tv.Tv
import com.tajmoti.libtulip.model.key.ItemKey
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.model.tmdb.TmdbItemId

inline fun HostedItem.toKey(): ItemKey.Hosted {
    return when (this) {
        is HostedItem.TvShow -> TvShowKey.Hosted(service, info.key)
        is HostedItem.Movie -> MovieKey.Hosted(service, info.key)
    }
}

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

inline fun SlimSeason.toKey(tv: Tv): SeasonKey.Tmdb {
    return SeasonKey.Tmdb(TvShowKey.Tmdb(TmdbItemId.Tv(tv.id)), seasonNumber)
}
