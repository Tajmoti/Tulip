package com.tajmoti.libtulip.model

import com.tajmoti.libtvprovider.TvItem

data class TulipTvShow(
    val service: StreamingService,
    val key: String,
    val name: String,
    val language: String
) {
    constructor(service: StreamingService, item: TvItem.Show)
            : this(service, item.key, item.name, item.language)

    val apiInfo: TvItem.Show.Info
        get() = TvItem.Show.Info(key, name, language)
}