package com.tajmoti.tulip.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import com.tajmoti.libtulip.model.StreamingService

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
    val number: Int?,
    val name: String?
)
