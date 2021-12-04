package com.tajmoti.tulip.db.entity.userdata

import androidx.room.Entity
import com.tajmoti.libtulip.model.hosted.StreamingService

@Entity(primaryKeys = ["streamingService", "tvShowId"])
data class DbLastPlayedPositionTvShowHosted(
    val streamingService: StreamingService,
    val tvShowId: String,
    val seasonNumber: Int,
    val episodeId: String,
    val progress: Float
)
