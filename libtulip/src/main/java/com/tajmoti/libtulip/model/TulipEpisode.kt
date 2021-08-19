package com.tajmoti.libtulip.model

import com.tajmoti.libtvprovider.Episode

data class TulipEpisode(
    val service: StreamingService,
    val tvShowKey: String,
    val seasonKey: String,
    val key: String,
    val number: Int?,
    val name: String?
) {
    constructor(service: StreamingService, tvShowKey: String, seasonKey: String, episode: Episode) :
            this(service, tvShowKey, seasonKey, episode.key, episode.number, episode.name)

    val apiInfo: Episode.Info
        get() = Episode.Info(key, number, name)
}