package com.tajmoti.tulip.entity.tmdb

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TmdbMovie(
    @PrimaryKey
    val id: Long,
    val name: String,
    val overview: String?,
    val posterPath: String?,
    val backdropPath: String?
)