package com.tajmoti.tulip.db.entity.userdata

import androidx.room.Entity

@Entity(primaryKeys = ["movieId"])
data class DbLastPlayedPositionMovieTmdb(
    val movieId: Long,
    val progress: Float
)
