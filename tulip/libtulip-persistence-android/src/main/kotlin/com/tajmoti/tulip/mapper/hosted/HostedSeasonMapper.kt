package com.tajmoti.tulip.mapper.hosted

import com.tajmoti.libtulip.model.Season
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.tulip.entity.hosted.HostedSeason
import com.tajmoti.tulip.mapper.Mapper

class HostedSeasonMapper : Mapper<Season.Hosted, HostedSeason> {

    override fun fromDb(db: HostedSeason): Season.Hosted = with(db) {
        val tvShowKey = TvShowKey.Hosted(service, tvShowKey)
        val key = SeasonKey.Hosted(tvShowKey, number)
        return Season.Hosted(key, number)
    }

    override fun toDb(repo: Season.Hosted): HostedSeason = with(repo) {
        return HostedSeason(key.tvShowKey.streamingService, key.tvShowKey.id, seasonNumber)
    }
}