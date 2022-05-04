package com.tajmoti.tulip.entity.user

import androidx.room.Entity
import com.tajmoti.libtulip.model.hosted.StreamingService

@Entity(primaryKeys = ["type", "streamingService", "key"])
data class FavoriteHostedItem(
    val type: ItemType,
    val streamingService: StreamingService,
    val key: String
)
