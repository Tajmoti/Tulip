package com.tajmoti.libtvprovider

data class TvShowInfo(
    val key: String,
    val info: TvItemInfo,
    val seasons: List<Season>
)