package com.tajmoti.libtulip.model

import com.tajmoti.libtvprovider.Season

data class TulipSeason(
    val service: StreamingService,
    val tvShowKey: String,
    val key: String,
    val number: Int
) {
    constructor(service: StreamingService, tvShowKey: String, season: Season)
            : this(service, tvShowKey, season.key, season.number)

    fun toApiInfo(dbEpisodes: List<TulipEpisode>): Season.Info {
        val epInfoList = dbEpisodes.map { it.apiInfo }
        return Season.Info(key, number, epInfoList)
    }
}