package com.tajmoti.tulip.db.entity

import androidx.room.Entity
import com.tajmoti.libtulip.model.StreamingService

@Entity(primaryKeys = ["service", "key"])
data class DbTvShow(
    val service: StreamingService,
    val key: String,
    val name: String,
    val language: String,
    val firstAirDateYear: Int?,
    val tmdbId: Long?
)