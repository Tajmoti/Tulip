package com.tajmoti.libtulip.model.search

import com.tajmoti.libtulip.model.hosted.MappedSearchResult
import com.tajmoti.libtulip.model.key.ItemKey
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.TvShowKey

sealed interface GroupedSearchResult {
    val tmdbId: ItemKey.Tmdb?
    val results: List<MappedSearchResult>


    class TvShow(
        override val tmdbId: TvShowKey.Tmdb,
        override val results: List<MappedSearchResult.TvShow>
    ) : GroupedSearchResult

    class Movie(
        override val tmdbId: MovieKey.Tmdb,
        override val results: List<MappedSearchResult.Movie>
    ) : GroupedSearchResult

    class UnrecognizedTvShow(
        override val results: List<MappedSearchResult.TvShow>,
    ) : GroupedSearchResult {
        override val tmdbId: TvShowKey.Tmdb? = null
    }

    class UnrecognizedMovie(
        override val results: List<MappedSearchResult.Movie>,
    ) : GroupedSearchResult {
        override val tmdbId: TvShowKey.Tmdb? = null
    }
}
