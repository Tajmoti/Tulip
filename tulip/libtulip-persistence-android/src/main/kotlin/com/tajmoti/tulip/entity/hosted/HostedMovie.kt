package com.tajmoti.tulip.entity.hosted

import androidx.room.Entity
import com.tajmoti.libtulip.model.key.StreamingService

@Entity(primaryKeys = ["service", "key"])
data class HostedMovie(
    val service: StreamingService,
    val key: String,
    val name: String,
    val language: String,
    val firstAirDateYear: Int?
)
