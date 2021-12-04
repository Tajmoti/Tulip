package com.tajmoti.tulip.db.entity.userdata

import androidx.room.Entity
import com.tajmoti.libtulip.model.hosted.StreamingService

@Entity(primaryKeys = ["streamingService", "movieId"])
data class DbLastPlayedPositionMovieHosted(
    val streamingService: StreamingService,
    val movieId: String,
    val progress: Float
)
