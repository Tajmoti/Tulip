package com.tajmoti.tulip.db.entity.tmdb

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    primaryKeys = ["tvId", "seasonNumber", "episodeNumber"],
    foreignKeys = [ForeignKey(
        entity = DbTmdbSeason::class,
        parentColumns = arrayOf("tvId", "seasonNumber"),
        childColumns = arrayOf("tvId", "seasonNumber")
    )]
)
data class DbTmdbEpisode(
    val tvId: Long,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val name: String,
    val overview: String?,
    val stillPath: String?,
    val voteAverage: Float?
)