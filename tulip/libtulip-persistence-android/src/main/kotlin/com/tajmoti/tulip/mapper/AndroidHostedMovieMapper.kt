package com.tajmoti.tulip.mapper

import com.tajmoti.libtulip.model.hosted.TvItemInfo
import com.tajmoti.libtulip.model.info.TulipMovie
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.tulip.db.entity.hosted.DbMovie

class AndroidHostedMovieMapper {

    fun fromDb(db: DbMovie, tmdbKey: MovieKey.Tmdb?): TulipMovie.Hosted = with(db) {
        val key = MovieKey.Hosted(db.service, db.key)
        return TulipMovie.Hosted(key, TvItemInfo(name, language, firstAirDateYear), tmdbKey)
    }

    fun toDb(repo: TulipMovie.Hosted): DbMovie = with(repo) {
        return DbMovie(key.streamingService, key.id, info.name, info.language, info.firstAirDateYear)
    }
}