package com.tajmoti.tulip.db.entity.hosted

import androidx.room.Entity
import androidx.room.ForeignKey
import com.tajmoti.libtulip.model.hosted.StreamingService

@Entity(
    primaryKeys = ["service", "tvShowKey", "number"],
    foreignKeys = [ForeignKey(
        entity = DbTvShow::class,
        parentColumns = arrayOf("service", "key"),
        childColumns = arrayOf("service", "tvShowKey")
    )]
)
data class DbSeason(
    val service: StreamingService,
    val tvShowKey: String,
    val number: Int
)
