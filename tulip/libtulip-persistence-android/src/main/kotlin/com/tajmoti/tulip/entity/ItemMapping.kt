package com.tajmoti.tulip.entity

import androidx.room.Entity
import com.tajmoti.libtulip.model.hosted.StreamingService

@Entity(primaryKeys = ["service", "key"])
data class ItemMapping(
    val service: StreamingService,
    val key: String,
    val tmdbId: Long
)