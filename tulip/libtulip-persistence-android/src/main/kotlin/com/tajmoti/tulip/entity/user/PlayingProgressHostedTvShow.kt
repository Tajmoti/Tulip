package com.tajmoti.tulip.entity.user

import androidx.room.Entity
import com.tajmoti.libtulip.model.hosted.StreamingService

@Entity(primaryKeys = ["streamingService", "tvShowId"])
data class PlayingProgressHostedTvShow(
    val streamingService: StreamingService,
    val tvShowId: String,
    val seasonNumber: Int,
    val episodeId: String,
    val progress: Float
)
