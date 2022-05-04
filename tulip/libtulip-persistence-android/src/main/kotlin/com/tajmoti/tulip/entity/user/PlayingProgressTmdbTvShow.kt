package com.tajmoti.tulip.entity.user

import androidx.room.Entity

@Entity(primaryKeys = ["tvShowId"])
data class PlayingProgressTmdbTvShow(
    val tvShowId: Long,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val progress: Float
)
