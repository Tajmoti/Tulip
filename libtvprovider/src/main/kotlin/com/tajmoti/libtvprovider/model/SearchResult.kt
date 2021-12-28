package com.tajmoti.libtvprovider.model

sealed interface SearchResult {
    /**
     * Key of this TV item. Only valid
     * on the streaming site where it came from.
     */
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
