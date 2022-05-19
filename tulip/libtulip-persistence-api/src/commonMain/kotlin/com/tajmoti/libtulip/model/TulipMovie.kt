package com.tajmoti.libtulip.model

import com.tajmoti.libtulip.model.key.MovieKey

sealed interface TulipMovie : StreamableInfo {
    override val name: String

    data class Tmdb(
        override val key: MovieKey.Tmdb,
        override val name: String,
        override val overview: String?,
        override val posterUrl: String?,
        override val backdropUrl: String?,
    ) : TulipMovie, TulipItem.Tmdb, StreamableInfo.Tmdb

    class Hosted(
        override val key: MovieKey.Hosted,
        val info: TvItemInfo,
        override val tmdbId: MovieKey.Tmdb?
    ) : TulipMovie, TulipItem.Hosted, StreamableInfo.Hosted {
        override val name = info.name
        override val language = LanguageCode(info.language)
    }
}