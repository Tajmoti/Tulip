package com.tajmoti.tulip.model

import androidx.room.Entity
import com.tajmoti.libtvprovider.TvItem

@Entity(primaryKeys = ["service", "key"])
data class DbMovie(
    val service: StreamingService,
    val key: String,
    val name: String,
    val language: String
) {
    constructor(service: StreamingService, movie: TvItem.Movie)
            : this(service, movie.key, movie.name, movie.language)

    val apiInfo: TvItem.Movie.Info
        get() = TvItem.Movie.Info(key, name, language)
}
