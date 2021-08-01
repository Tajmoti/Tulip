package com.tajmoti.tulip.model

import androidx.room.Entity

@Entity(
    primaryKeys = ["service", "key"]
)
data class DbTvShow(
    val service: StreamingService,
    val key: String,
    val name: String
)
