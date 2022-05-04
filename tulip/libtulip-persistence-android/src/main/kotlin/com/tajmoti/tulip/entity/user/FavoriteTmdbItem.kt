package com.tajmoti.tulip.entity.user

import androidx.room.Entity

@Entity(primaryKeys = ["type", "tmdbItemId"])
data class FavoriteTmdbItem(
    val type: ItemType,
    val tmdbItemId: Long
)
