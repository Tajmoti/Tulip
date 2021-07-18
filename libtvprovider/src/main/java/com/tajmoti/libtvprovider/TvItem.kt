package com.tajmoti.libtvprovider

import com.tajmoti.libtvprovider.show.Season
import com.tajmoti.libtvprovider.stream.Streamable

sealed interface TvItem : Marshallable, NamedItem {

    interface Show : TvItem {
        suspend fun fetchSeasons(): Result<List<Season>>
    }

    interface Movie : TvItem, Streamable
}
