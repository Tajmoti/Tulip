package com.tajmoti.libtulip.ui.library

import com.tajmoti.libtulip.model.IdentityItem
import com.tajmoti.libtulip.model.history.LastPlayedPosition
import com.tajmoti.libtulip.model.key.ItemKey
import java.io.Serializable

data class LibraryItem(
    override val key: ItemKey,
    val name: String,
    val posterPath: String?,
    val lastPlayedPosition: LastPlayedPosition?
) : IdentityItem<ItemKey>, Serializable