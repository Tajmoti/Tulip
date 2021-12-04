package com.tajmoti.tulip.db.entity.hosted

import androidx.room.Entity
import com.tajmoti.libtulip.model.hosted.StreamingService

@Entity(primaryKeys = ["service", "key"])
data class DbTmdbMapping(
    val service: StreamingService,
    val key: String,
    val tmdbId: Long
)