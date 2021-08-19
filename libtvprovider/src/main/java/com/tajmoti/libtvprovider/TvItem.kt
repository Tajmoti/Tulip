package com.tajmoti.libtvprovider

sealed interface TvItem : Marshallable, NamedItem {
    /**
     * ISO 639-1 language code
     */
    val language: String

    /**
     * The year when this show or movie was first aired.
     */
    val firstAirDateYear: Int?

    interface Show : TvItem {
        suspend fun fetchSeasons(): Result<List<Season>>

        data class Info(
            override val key: String,
            override val name: String,
            override val language: String,
            override val firstAirDateYear: Int?
        ) : TvItem.Info
    }

    interface Movie : TvItem, Streamable {
        data class Info(
            override val key: String,
            override val name: String,
            override val language: String,
            override val firstAirDateYear: Int?
        ) : TvItem.Info
    }

    sealed interface Info : Marshallable {
        val name: String
        val language: String
        val firstAirDateYear: Int?
    }
}
