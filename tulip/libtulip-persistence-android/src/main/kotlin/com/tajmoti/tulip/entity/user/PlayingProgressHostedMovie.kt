package com.tajmoti.tulip.entity.user

import androidx.room.Entity
import com.tajmoti.libtulip.model.key.StreamingService

@Entity(primaryKeys = ["streamingService", "movieId"])
data class PlayingProgressHostedMovie(
    val streamingService: StreamingService,
    val movieId: String,
    val progress: Float
)
