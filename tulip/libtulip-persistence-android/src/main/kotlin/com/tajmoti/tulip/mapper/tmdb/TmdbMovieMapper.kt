package com.tajmoti.tulip.mapper.tmdb

import com.tajmoti.libtulip.model.info.TulipMovie
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.tulip.entity.tmdb.TmdbMovie
import com.tajmoti.tulip.mapper.Mapper

class TmdbMovieMapper : Mapper<TulipMovie.Tmdb, TmdbMovie> {

    override fun fromDb(db: TmdbMovie): TulipMovie.Tmdb = with(db) {
        val key = MovieKey.Tmdb(id)
        return TulipMovie.Tmdb(key, name, overview, posterPath, backdropPath)
    }

    override fun toDb(repo: TulipMovie.Tmdb): TmdbMovie {
        return TmdbMovie(repo.key.id, repo.name, repo.overview, repo.posterUrl, repo.backdropUrl)
    }
}