package com.tajmoti.tulip.mapper.tmdb

import com.tajmoti.libtulip.model.info.Season
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.tulip.entity.tmdb.TmdbSeason
import com.tajmoti.tulip.mapper.Mapper

class TmdbSeasonMapper : Mapper<Season.Tmdb, TmdbSeason> {

    override fun fromDb(db: TmdbSeason): Season.Tmdb = with(db) {
        val tvShowKey = TvShowKey.Tmdb(db.tvId)
        val seasonKey = SeasonKey.Tmdb(tvShowKey, seasonNumber)
        return Season.Tmdb(seasonKey, name, seasonNumber, overview)
    }

    override fun toDb(repo: Season.Tmdb): TmdbSeason = with(repo) {
        return TmdbSeason(key.tvShowKey.id, name, overview, seasonNumber)
    }
}