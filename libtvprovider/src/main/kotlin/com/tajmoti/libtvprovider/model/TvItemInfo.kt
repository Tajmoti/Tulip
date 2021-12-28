package com.tajmoti.libtvprovider.model

data class TvItemInfo(
    /**
     * Full name of the item
     */
    val name: String,
    /**
     * ISO 639-1 language code
     */
    val language: String,
    /**
     * The year when this show or movie was first aired.
     */
    val firstAirDateYear: Int?
)