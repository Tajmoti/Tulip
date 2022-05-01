package com.tajmoti.tulip.mapper

import com.tajmoti.libtulip.model.info.Season
import com.tajmoti.libtulip.model.info.TvShow
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.tulip.db.entity.tmdb.DbTmdbTv

class AndroidTmdbTvShowMapper {

    fun fromDb(db: DbTmdbTv, seasons: List<Season.Tmdb>): TvShow.Tmdb = with(db) {
        val key = TvShowKey.Tmdb(id)
        return TvShow.Tmdb(key, name, null, posterPath, backdropPath, seasons)
    }

    fun toDb(repo: TvShow.Tmdb): DbTmdbTv = with(repo) {
        return DbTmdbTv(key.id, name, posterUrl, backdropUrl)
    }
}