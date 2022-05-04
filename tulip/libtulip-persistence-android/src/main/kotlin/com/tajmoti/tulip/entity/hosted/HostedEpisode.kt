package com.tajmoti.tulip.entity.hosted

import androidx.room.Entity
import androidx.room.ForeignKey
import com.tajmoti.libtulip.model.hosted.StreamingService

@Entity(
    primaryKeys = ["service", "tvShowKey", "seasonNumber", "key"],
    foreignKeys = [ForeignKey(
        entity = HostedSeason::class,
        parentColumns = arrayOf("service", "tvShowKey", "number"),
        childColumns = arrayOf("service", "tvShowKey", "seasonNumber")
    )]
)
data class HostedEpisode(
    val service: StreamingService,
    val tvShowKey: String,
    val seasonNumber: Int,
    val key: String,
    val number: Int,
    val name: String?,
    val overview: String?,
    val stillPath: String?
)
