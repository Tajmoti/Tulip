package com.tajmoti.tulip.mapper.hosted

import com.tajmoti.libtulip.model.hosted.TvItemInfo
import com.tajmoti.libtulip.model.info.TulipMovie
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.tulip.entity.hosted.HostedMovie

class HostedMovieMapper {

    fun fromDb(db: HostedMovie, tmdbKey: MovieKey.Tmdb?): TulipMovie.Hosted = with(db) {
        val key = MovieKey.Hosted(db.service, db.key)
        return TulipMovie.Hosted(key, TvItemInfo(name, language, firstAirDateYear), tmdbKey)
    }

    fun toDb(repo: TulipMovie.Hosted): HostedMovie = with(repo) {
        return HostedMovie(key.streamingService, key.id, info.name, info.language, info.firstAirDateYear)
    }
}