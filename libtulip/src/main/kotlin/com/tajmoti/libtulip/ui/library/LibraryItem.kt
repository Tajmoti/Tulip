package com.tajmoti.libtulip.ui.library

import com.tajmoti.libtulip.model.history.LastPlayedPosition
import com.tajmoti.libtulip.model.key.ItemKey
import java.io.Serializable

data class LibraryItem(
    val key: ItemKey,
    val name: String,
    val posterPath: String?,
    val lastPlayedPosition: LastPlayedPosition?
) : Serializable