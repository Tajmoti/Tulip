package com.tajmoti.libtulip.model.search

import com.tajmoti.libtulip.model.IdentityItem
import com.tajmoti.libtulip.model.hosted.MappedSearchResult
import com.tajmoti.libtulip.model.key.ItemKey
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.TvShowKey

sealed interface GroupedSearchResult: IdentityItem<Any> {
    val tmdbId: ItemKey.Tmdb?
    val results: List<MappedSearchResult>

    class TvShow(
        override val tmdbId: TvShowKey.Tmdb,
        override val results: List<MappedSearchResult.TvShow>
    ) : GroupedSearchResult {
        override val key = tmdbId
    }

    class Movie(
        override val tmdbId: MovieKey.Tmdb,
        override val results: List<MappedSearchResult.Movie>
    ) : GroupedSearchResult {
        override val key = tmdbId
    }

    class UnrecognizedTvShow(
        override val results: List<MappedSearchResult.TvShow>,
    ) : GroupedSearchResult {
        override val tmdbId: TvShowKey.Tmdb? = null
        override val key = UnrecognizedTvShow::class
    }

    class UnrecognizedMovie(
        override val results: List<MappedSearchResult.Movie>,
    ) : GroupedSearchResult {
        override val tmdbId: TvShowKey.Tmdb? = null
        override val key = UnrecognizedMovie::class
    }
}
