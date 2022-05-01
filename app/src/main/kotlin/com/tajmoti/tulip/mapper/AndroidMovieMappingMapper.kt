package com.tajmoti.tulip.mapper

import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.tulip.db.entity.hosted.DbTmdbMapping

class AndroidMovieMappingMapper {

    fun fromDb(db: DbTmdbMapping): MovieKey.Hosted = with(db) {
        return MovieKey.Hosted(service, key)
    }

    fun toDb(repo: MovieKey.Hosted, tmdbKey: MovieKey.Tmdb): DbTmdbMapping = with(repo) {
        return DbTmdbMapping(streamingService, id, tmdbKey.id)
    }
}