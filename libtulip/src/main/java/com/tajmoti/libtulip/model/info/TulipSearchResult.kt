package com.tajmoti.libtulip.model.info

import com.tajmoti.libtulip.model.hosted.HostedItem

sealed class TulipSearchResult {
    abstract val tmdbId: TmdbItemId?
    abstract val results: List<HostedItem>


    class TvShow(
        override val tmdbId: TmdbItemId.Tv?,
        override val results: List<HostedItem.TvShow>
    ) : TulipSearchResult()

    class Movie(
        override val tmdbId: TmdbItemId.Movie?,
        override val results: List<HostedItem.Movie>
    ) : TulipSearchResult()

    class Unrecognized(
        override val results: List<HostedItem>
    ) : TulipSearchResult() {
        override val tmdbId: TmdbItemId? = null
    }
}
