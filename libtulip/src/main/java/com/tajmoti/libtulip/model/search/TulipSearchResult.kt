package com.tajmoti.libtulip.model.search

import com.tajmoti.libtulip.model.hosted.MappedSearchResult
import com.tajmoti.libtulip.model.tmdb.TmdbItemId

sealed class TulipSearchResult {
    abstract val tmdbId: TmdbItemId?
    abstract val results: List<MappedSearchResult>


    class TvShow(
        override val tmdbId: TmdbItemId.Tv?,
        override val results: List<MappedSearchResult.TvShow>
    ) : TulipSearchResult()

    class Movie(
        override val tmdbId: TmdbItemId.Movie?,
        override val results: List<MappedSearchResult.Movie>
    ) : TulipSearchResult()

    class Unrecognized(
        override val results: List<MappedSearchResult>
    ) : TulipSearchResult() {
        override val tmdbId: TmdbItemId? = null
    }
}
