package com.tajmoti.tulip.model

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    primaryKeys = ["service", "tvShowKey", "seasonKey", "key"],
    foreignKeys = [ForeignKey(
        entity = DbSeason::class,
        parentColumns = arrayOf("service", "tvShowKey", "key"),
        childColumns = arrayOf("service", "tvShowKey", "seasonKey"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class DbEpisode(
    val service: StreamingService,
    val tvShowKey: String,
    val seasonKey: String,
    val key: String,
    val name: String
)
