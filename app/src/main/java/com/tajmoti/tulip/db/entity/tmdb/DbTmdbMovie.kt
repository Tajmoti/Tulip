package com.tajmoti.tulip.db.entity.tmdb

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DbTmdbMovie(
    @PrimaryKey
    val id: Long,
    val name: String,
    val overview: String?,
    val posterPath: String?
)