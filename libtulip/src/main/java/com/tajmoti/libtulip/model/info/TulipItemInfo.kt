package com.tajmoti.libtulip.model.info

import com.tajmoti.libtulip.model.key.ItemKey

data class TulipItemInfo(
    val key: ItemKey,
    val name: String,
    val posterPath: String?
)