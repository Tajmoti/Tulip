package com.tajmoti.tulip.mapper

import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.tulip.db.entity.hosted.DbTmdbMapping

class AndroidTvShowMappingMapper {

    fun fromDb(db: DbTmdbMapping): TvShowKey.Hosted = with(db) {
        return TvShowKey.Hosted(service, key)
    }

    fun toDb(repo: TvShowKey.Hosted, tmdbKey: TvShowKey.Tmdb): DbTmdbMapping = with(repo) {
        return DbTmdbMapping(streamingService, id, tmdbKey.id)
    }
}