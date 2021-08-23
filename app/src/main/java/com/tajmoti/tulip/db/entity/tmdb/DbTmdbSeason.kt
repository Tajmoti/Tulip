package com.tajmoti.tulip.db.entity.tmdb

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    primaryKeys = ["tvId", "seasonNumber"],
    foreignKeys = [ForeignKey(
        entity = DbTmdbTv::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("tvId")
    )]
)
data class DbTmdbSeason(
    val tvId: Long,
    val name: String,
    val overview: String?,
    val seasonNumber: Int
)
