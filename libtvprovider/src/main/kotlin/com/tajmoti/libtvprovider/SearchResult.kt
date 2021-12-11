package com.tajmoti.libtvprovider

sealed interface SearchResult {
    val key: String

    /**
     * Information about this search result
     */
    val info: TvItemInfo

    data class TvShow(
        override val key: String,
        override val info: TvItemInfo
    ) : SearchResult

    data class Movie(
        override val key: String,
        override val info: TvItemInfo
    ) : SearchResult
}
