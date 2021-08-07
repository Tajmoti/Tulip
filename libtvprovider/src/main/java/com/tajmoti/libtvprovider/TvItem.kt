package com.tajmoti.libtvprovider

sealed interface TvItem : Marshallable, NamedItem {
    /**
     * ISO 639-1 language code
     */
    val language: String

    interface Show : TvItem {
        suspend fun fetchSeasons(): Result<List<Season>>

        data class Info(
            override val key: String,
            override val name: String,
            override val language: String
        ) : TvItem.Info
    }

    interface Movie : TvItem, Streamable {
        data class Info(
            override val key: String,
            override val name: String,
            override val language: String
        ) : TvItem.Info
    }

    sealed interface Info : Marshallable {
        val name: String
        val language: String
    }
}
