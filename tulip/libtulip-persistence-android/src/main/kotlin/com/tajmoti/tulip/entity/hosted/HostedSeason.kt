package com.tajmoti.tulip.entity.hosted

import androidx.room.Entity
import androidx.room.ForeignKey
import com.tajmoti.libtulip.model.hosted.StreamingService

@Entity(
    primaryKeys = ["service", "tvShowKey", "number"],
    foreignKeys = [ForeignKey(
        entity = HostedTvShow::class,
        parentColumns = arrayOf("service", "key"),
        childColumns = arrayOf("service", "tvShowKey")
    )]
)
data class HostedSeason(
    val service: StreamingService,
    val tvShowKey: String,
    val number: Int
)
