package com.tajmoti.tulip.entity.tmdb

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    primaryKeys = ["tvId", "seasonNumber", "episodeNumber"],
    foreignKeys = [ForeignKey(
        entity = TmdbSeason::class,
        parentColumns = arrayOf("tvId", "seasonNumber"),
        childColumns = arrayOf("tvId", "seasonNumber")
    )]
)
data class TmdbEpisode(
    val tvId: Long,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val name: String,
    val overview: String?,
    val stillPath: String?,
    val voteAverage: Float?
)