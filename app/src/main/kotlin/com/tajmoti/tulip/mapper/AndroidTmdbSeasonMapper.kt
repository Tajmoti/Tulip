package com.tajmoti.tulip.mapper

import com.tajmoti.libtulip.model.info.Season
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.tulip.db.entity.tmdb.DbTmdbSeason

class AndroidTmdbSeasonMapper : Mapper<Season.Tmdb, DbTmdbSeason> {

    override fun fromDb(db: DbTmdbSeason): Season.Tmdb = with(db) {
        val tvShowKey = TvShowKey.Tmdb(db.tvId)
        val seasonKey = SeasonKey.Tmdb(tvShowKey, seasonNumber)
        return Season.Tmdb(seasonKey, name, seasonNumber, overview)
    }

    override fun toDb(repo: Season.Tmdb): DbTmdbSeason = with(repo) {
        return DbTmdbSeason(key.tvShowKey.id, name, overview, seasonNumber)
    }
}