package com.tajmoti.tulip.db.entity.userdata

import androidx.room.Entity
import com.tajmoti.libtulip.model.info.ItemType

@Entity(primaryKeys = ["type", "tmdbItemId"])
data class DbFavoriteTmdbItem(
    val type: ItemType,
    val tmdbItemId: Long
)
