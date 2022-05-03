package com.tajmoti.libtvprovider.model

sealed interface TvItem {
    /**
     * Information about this item.
     */
    val info: TvItemInfo

    data class TvShow(
        override val info: TvItemInfo,
        val seasons: List<Season>
    ): TvItem

    data class Movie(
        override val info: TvItemInfo
    ): TvItem
}