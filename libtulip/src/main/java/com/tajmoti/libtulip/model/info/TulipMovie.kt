package com.tajmoti.libtulip.model.info

import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.tmdb.TmdbItemId
import com.tajmoti.libtvprovider.TvItemInfo

sealed interface TulipMovie : StreamableInfo {
    override val name: String

    data class Tmdb(
        override val key: MovieKey.Tmdb,
        override val name: String,
        override val overview: String?,
        override val posterPath: String?,
        override val backdropPath: String?,
    ) : TulipMovie, TulipItem.Tmdb, StreamableInfo.Tmdb

    class Hosted(
        override val key: MovieKey.Hosted,
        val info: TvItemInfo,
        override val tmdbId: TmdbItemId.Movie?
    ) : TulipMovie, TulipItem.Hosted, StreamableInfo.Hosted {
        override val name = info.name
        override val language = LanguageCode(info.language)
    }
}