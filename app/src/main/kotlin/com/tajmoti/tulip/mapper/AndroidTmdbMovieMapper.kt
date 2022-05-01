package com.tajmoti.tulip.mapper

import com.tajmoti.libtulip.model.info.TulipMovie
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.tulip.db.entity.tmdb.DbTmdbMovie

class AndroidTmdbMovieMapper : Mapper<TulipMovie.Tmdb, DbTmdbMovie> {

    override fun fromDb(db: DbTmdbMovie): TulipMovie.Tmdb = with(db) {
        val key = MovieKey.Tmdb(id)
        return TulipMovie.Tmdb(key, name, overview, posterPath, backdropPath)
    }

    override fun toDb(repo: TulipMovie.Tmdb): DbTmdbMovie {
        return DbTmdbMovie(repo.key.id, repo.name, repo.overview, repo.posterUrl, repo.backdropUrl)
    }
}