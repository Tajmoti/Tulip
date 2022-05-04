package com.tajmoti.tulip.entity.user

import androidx.room.Entity

@Entity(primaryKeys = ["movieId"])
data class PlayingProgressTmdbMovie(
    val movieId: Long,
    val progress: Float
)
