package com.tajmoti.tulip.model

import androidx.room.Entity
import com.tajmoti.libtvprovider.TvItem

@Entity(primaryKeys = ["service", "key"])
data class DbTvShow(
    val service: StreamingService,
    val key: String,
    val name: String,
    val language: String
) {
    constructor(service: StreamingService, item: TvItem.Show)
            : this(service, item.key, item.name, item.language)

    val apiInfo: TvItem.Show.Info
        get() = TvItem.Show.Info(key, name, language)
}