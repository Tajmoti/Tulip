package com.tajmoti.tulip.db.entity.userdata

import androidx.room.Entity
import com.tajmoti.libtulip.model.hosted.StreamingService
import com.tajmoti.libtulip.model.info.ItemType

@Entity(primaryKeys = ["type", "streamingService", "key"])
data class DbFavoriteHostedItem(
    val type: ItemType,
    val streamingService: StreamingService,
    val key: String
)
