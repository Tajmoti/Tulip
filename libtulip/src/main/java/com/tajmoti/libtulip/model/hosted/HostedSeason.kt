package com.tajmoti.libtulip.model.hosted

import com.tajmoti.libtvprovider.Season

data class HostedSeason(
    val service: StreamingService,
    val tvShowKey: String,
    val number: Int
) {
    constructor(service: StreamingService, season: Season)
            : this(service, season.tvShowKey, season.number)

    fun toApiInfo(dbEpisodes: List<HostedEpisode>): Season {
        val epInfoList = dbEpisodes.map { it.apiInfo }
        return Season(tvShowKey, number, epInfoList)
    }
}