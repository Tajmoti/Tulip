package com.tajmoti.tulip.mapper

import com.tajmoti.libtulip.model.info.Season
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.tulip.db.entity.hosted.DbSeason

class AndroidHostedSeasonMapper : Mapper<Season.Hosted, DbSeason> {

    override fun fromDb(db: DbSeason): Season.Hosted = with(db) {
        val tvShowKey = TvShowKey.Hosted(service, tvShowKey)
        val key = SeasonKey.Hosted(tvShowKey, number)
        return Season.Hosted(key, number)
    }

    override fun toDb(repo: Season.Hosted): DbSeason = with(repo) {
        return DbSeason(key.tvShowKey.streamingService, key.tvShowKey.id, seasonNumber)
    }
}