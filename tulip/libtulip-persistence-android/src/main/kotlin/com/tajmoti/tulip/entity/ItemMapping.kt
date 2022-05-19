package com.tajmoti.tulip.entity

import androidx.room.Entity
import com.tajmoti.libtulip.model.key.StreamingService

@Entity(primaryKeys = ["service", "key"])
data class ItemMapping(
    val service: StreamingService,
    val key: String,
    val tmdbId: Long
)