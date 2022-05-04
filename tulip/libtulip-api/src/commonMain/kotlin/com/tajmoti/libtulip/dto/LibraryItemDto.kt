package com.tajmoti.libtulip.dto

import com.tajmoti.libtulip.model.IdentityItem
import com.tajmoti.libtulip.model.key.ItemKey

data class LibraryItemDto(
    override val key: ItemKey,
    val name: String,
    val posterPath: String?,
    val playingProgress: LibraryItemPlayingProgressDto?
) : IdentityItem<ItemKey>