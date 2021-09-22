package com.tajmoti.tulip.db.entity.userdata

import androidx.room.Entity

@Entity(primaryKeys = ["tvShowId"])
data class DbLastPlayedPositionTmdb(
    val tvShowId: Long,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val progress: Float?
)
