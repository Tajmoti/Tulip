package com.tajmoti.libtulip.dto

import com.tajmoti.libtulip.model.IdentityItem
import com.tajmoti.libtulip.model.key.ItemKey
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.TvShowKey

sealed interface SearchResultDto : IdentityItem<Any> {
    val tmdbId: ItemKey.Tmdb?
    val results: List<SearchResultGroupDto>

    class TvShow(
        override val tmdbId: TvShowKey.Tmdb,
        override val results: List<SearchResultGroupDto.TvShow>
    ) : SearchResultDto {
        override val key = tmdbId
    }

    class Movie(
        override val tmdbId: MovieKey.Tmdb,
        override val results: List<SearchResultGroupDto.Movie>
    ) : SearchResultDto {
        override val key = tmdbId
    }

    class UnrecognizedTvShow(
        override val results: List<SearchResultGroupDto.TvShow>,
    ) : SearchResultDto {
        override val tmdbId: TvShowKey.Tmdb? = null
        override val key = UnrecognizedTvShow::class
    }

    class UnrecognizedMovie(
        override val results: List<SearchResultGroupDto.Movie>,
    ) : SearchResultDto {
        override val tmdbId: TvShowKey.Tmdb? = null
        override val key = UnrecognizedMovie::class
    }
}
