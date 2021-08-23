package com.tajmoti.libtulip.model.hosted

import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtvprovider.EpisodeInfo

data class HostedEpisode(
    val service: StreamingService,
    val tvShowKey: String,
    val seasonNumber: Int,
    val key: String,
    val number: Int,
    val name: String?
) : HostedStreamable {
    constructor(
        service: StreamingService,
        tvShowKey: String,
        seasonNumber: Int,
        episode: EpisodeInfo
    ) :
            this(service, tvShowKey, seasonNumber, episode.key, episode.number, episode.name)

    val apiInfo: EpisodeInfo
        get() = EpisodeInfo(key, number, name)

    override val hostedKey = EpisodeKey.Hosted(
        SeasonKey.Hosted(TvShowKey.Hosted(service, tvShowKey), seasonNumber), key
    )
}