package com.tajmoti.tulip.model

import androidx.room.Entity

@Entity(primaryKeys = ["service", "key"])
data class DbMovie(
    val service: StreamingService,
    val key: String,
    val name: String
)
