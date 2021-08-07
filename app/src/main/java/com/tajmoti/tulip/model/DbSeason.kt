package com.tajmoti.tulip.model

import androidx.room.Entity
import androidx.room.ForeignKey
import com.tajmoti.libtvprovider.Season

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
) {
    constructor(service: StreamingService, tvShowKey: String, season: Season)
            : this(service, tvShowKey, season.key, season.number)

    fun toApiInfo(dbEpisodes: List<DbEpisode>): Season.Info {
        val epInfoList = dbEpisodes.map { it.apiInfo }
        return Season.Info(key, number, epInfoList)
    }
}
