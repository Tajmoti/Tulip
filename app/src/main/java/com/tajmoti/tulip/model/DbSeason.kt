package com.tajmoti.tulip.model

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    primaryKeys = ["service", "tvShowKey", "key"],
    foreignKeys = [ForeignKey(
        entity = DbTvShow::class,
        parentColumns = arrayOf("service", "key"),
        childColumns = arrayOf("service", "tvShowKey"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class DbSeason(
    val service: StreamingService,
    val tvShowKey: String,
    val key: String,
    val number: Int
)
