package com.tajmoti.libtvprovider

data class TvItemInfo(
        /**
         * ID of this TV Show or Movie
         */
        val id: String,
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