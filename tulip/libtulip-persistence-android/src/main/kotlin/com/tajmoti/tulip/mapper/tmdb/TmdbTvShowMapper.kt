package com.tajmoti.tulip.mapper.tmdb

import com.tajmoti.libtulip.model.info.Season
import com.tajmoti.libtulip.model.info.TvShow
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.tulip.entity.tmdb.TmdbTvShow

class TmdbTvShowMapper {

    fun fromDb(db: TmdbTvShow, seasons: List<Season.Tmdb>): TvShow.Tmdb = with(db) {
        val key = TvShowKey.Tmdb(id)
        return TvShow.Tmdb(key, name, null, posterPath, backdropPath, seasons)
    }

    fun toDb(repo: TvShow.Tmdb): TmdbTvShow = with(repo) {
        return TmdbTvShow(key.id, name, posterUrl, backdropUrl)
    }
}