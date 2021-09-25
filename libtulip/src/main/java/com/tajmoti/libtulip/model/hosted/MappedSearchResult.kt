package com.tajmoti.libtulip.model.hosted

import com.tajmoti.libtulip.model.key.ItemKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.model.tmdb.TmdbItemId
import com.tajmoti.libtvprovider.TvItemInfo

sealed interface MappedSearchResult {
    val key: ItemKey.Hosted
    val info: TvItemInfo
    val tmdbId: TmdbItemId?

    data class TvShow(
        override val key: TvShowKey.Hosted,
        override val info: TvItemInfo,
        override val tmdbId: TmdbItemId.Tv?,
    ) : MappedSearchResult

    data class Movie(
        override val key: ItemKey.Hosted,
        override val info: TvItemInfo,
        override val tmdbId: TmdbItemId.Movie?
    ) : MappedSearchResult
}
