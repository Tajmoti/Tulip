package com.tajmoti.tulip.db.entity.hosted

import androidx.room.Entity
import androidx.room.ForeignKey
import com.tajmoti.libtulip.model.hosted.StreamingService

@Entity(
    primaryKeys = ["service", "tvShowKey", "seasonNumber", "key"],
    foreignKeys = [ForeignKey(
        entity = DbSeason::class,
        parentColumns = arrayOf("service", "tvShowKey", "number"),
        childColumns = arrayOf("service", "tvShowKey", "seasonNumber")
    )]
)
data class DbEpisode(
    val service: StreamingService,
    val tvShowKey: String,
    val seasonNumber: Int,
    val key: String,
    val number: Int,
    val name: String?
)
