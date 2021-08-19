package com.tajmoti.tulip.model

import com.tajmoti.libtvprovider.Episode
import com.tajmoti.libtvprovider.Streamable
import com.tajmoti.libtvprovider.TvItem

sealed class StreamableInfo(
    val streamable: Streamable
) {

    data class TvShow(
        val show: DbTvShow,
        val season: DbSeason,
        val episode: Episode,
    ) : StreamableInfo(episode)

    data class Movie(
        val movie: TvItem.Movie
    ) : StreamableInfo(movie)
}