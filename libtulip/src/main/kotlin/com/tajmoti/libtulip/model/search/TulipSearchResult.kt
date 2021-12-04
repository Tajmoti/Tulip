package com.tajmoti.libtulip.model.search

import com.tajmoti.libtulip.model.hosted.MappedSearchResult
import com.tajmoti.libtulip.model.key.ItemKey
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.TvShowKey

sealed class TulipSearchResult {
    abstract val tmdbId: ItemKey.Tmdb?
    abstract val results: List<MappedSearchResult>


    class TvShow(
        override val tmdbId: TvShowKey.Tmdb,
        override val results: List<MappedSearchResult.TvShow>
    ) : TulipSearchResult()

    class Movie(
        override val tmdbId: MovieKey.Tmdb,
        override val results: List<MappedSearchResult.Movie>
    ) : TulipSearchResult()

    class Unrecognized(
        override val results: List<MappedSearchResult>,
    ) : TulipSearchResult() {
        override val tmdbId: TvShowKey.Tmdb? = null
    }
}
