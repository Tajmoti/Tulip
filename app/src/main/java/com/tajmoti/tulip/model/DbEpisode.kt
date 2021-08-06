package com.tajmoti.tulip.model

import androidx.room.Entity
import androidx.room.ForeignKey
import com.tajmoti.libtvprovider.show.Episode

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
) {
    constructor(service: StreamingService, tvShowKey: String, seasonKey: String, episode: Episode) :
            this(service, tvShowKey, seasonKey, episode.key, episode.number, episode.name)

    val apiInfo: Episode.Info
        get() = Episode.Info(key, number, name)
}
