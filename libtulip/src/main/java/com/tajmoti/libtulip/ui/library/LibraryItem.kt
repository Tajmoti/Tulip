package com.tajmoti.libtulip.ui.library

import com.tajmoti.libtulip.model.key.ItemKey

data class LibraryItem(
    val key: ItemKey,
    val name: String,
    val posterPath: String?
)