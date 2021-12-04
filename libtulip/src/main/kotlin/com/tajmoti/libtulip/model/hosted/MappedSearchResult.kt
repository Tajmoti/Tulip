package com.tajmoti.libtulip.model.hosted

import com.tajmoti.libtulip.model.key.ItemKey
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtvprovider.TvItemInfo

sealed interface MappedSearchResult {
    val key: ItemKey.Hosted
    val info: TvItemInfo
    val tmdbId: ItemKey.Tmdb?

    data class TvShow(
        override val key: TvShowKey.Hosted,
        override val info: TvItemInfo,
        override val tmdbId: TvShowKey.Tmdb?,
    ) : MappedSearchResult

    data class Movie(
        override val key: MovieKey.Hosted,
        override val info: TvItemInfo,
        override val tmdbId: MovieKey.Tmdb?
    ) : MappedSearchResult
}
