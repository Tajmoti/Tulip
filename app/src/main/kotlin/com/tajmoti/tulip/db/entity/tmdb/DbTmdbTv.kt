package com.tajmoti.tulip.db.entity.tmdb

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DbTmdbTv(
    @PrimaryKey
    val id: Long,
    val name: String,
    val posterPath: String?,
    val backdropPath: String?
)
