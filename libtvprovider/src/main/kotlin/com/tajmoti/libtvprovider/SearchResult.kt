package com.tajmoti.libtvprovider

data class SearchResult(
    val key: String,
    /**
     * Whether this is a TV show or a movie
     */
    val type: Type,
    /**
     * Information about this search result
     */
    val info: TvItemInfo
) {

    enum class Type {
        TV_SHOW,
        MOVIE
    }
}
