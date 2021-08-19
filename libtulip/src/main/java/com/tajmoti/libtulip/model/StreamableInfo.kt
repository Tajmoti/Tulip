package com.tajmoti.libtulip.model

import com.tajmoti.libtvprovider.Episode
import com.tajmoti.libtvprovider.Streamable
import com.tajmoti.libtvprovider.TvItem

sealed class StreamableInfo(
    val streamable: Streamable
) {

    data class TvShow(
        val show: TulipTvShow,
        val season: TulipSeason,
        val episode: Episode,
    ) : StreamableInfo(episode)

    data class Movie(
        val movie: TvItem.Movie
    ) : StreamableInfo(movie)
}