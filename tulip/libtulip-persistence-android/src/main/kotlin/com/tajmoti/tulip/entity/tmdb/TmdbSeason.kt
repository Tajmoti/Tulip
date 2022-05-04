package com.tajmoti.tulip.entity.tmdb

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    primaryKeys = ["tvId", "seasonNumber"],
    foreignKeys = [ForeignKey(
        entity = TmdbTvShow::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("tvId")
    )]
)
data class TmdbSeason(
    val tvId: Long,
    val name: String,
    val overview: String?,
    val seasonNumber: Int
)
