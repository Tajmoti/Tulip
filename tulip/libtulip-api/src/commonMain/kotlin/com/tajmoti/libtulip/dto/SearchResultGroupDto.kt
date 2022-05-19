package com.tajmoti.libtulip.dto

import com.tajmoti.libtulip.model.key.ItemKey
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.TvShowKey

sealed interface SearchResultGroupDto {
    val key: ItemKey.Hosted
    val info: TvItemInfoDto
    val tmdbId: ItemKey.Tmdb?

    data class TvShow(
        override val key: TvShowKey.Hosted,
        override val info: TvItemInfoDto,
        override val tmdbId: TvShowKey.Tmdb?,
    ) : SearchResultGroupDto

    data class Movie(
        override val key: MovieKey.Hosted,
        override val info: TvItemInfoDto,
        override val tmdbId: MovieKey.Tmdb?
    ) : SearchResultGroupDto
}
