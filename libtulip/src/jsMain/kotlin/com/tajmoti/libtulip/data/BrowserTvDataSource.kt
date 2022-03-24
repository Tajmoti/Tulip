package com.tajmoti.libtulip.data

import com.tajmoti.libtulip.model.info.TulipMovie
import com.tajmoti.libtulip.model.info.SeasonWithEpisodes
import com.tajmoti.libtulip.model.info.TvShow
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.model.key.TvShowKey
import kotlinx.coroutines.flow.Flow

class BrowserTvDataSource : LocalTvDataSource {
    private val tvStorage = BrowserStorage<TvShowKey.Tmdb, TvShow.Tmdb>("5")
    private val seasonStorage = BrowserStorage<SeasonKey.Tmdb, SeasonWithEpisodes.Tmdb>("7")
    private val movieStorage = BrowserStorage<MovieKey.Tmdb, TulipMovie.Tmdb>("6")


    override fun getTvShow(key: TvShowKey.Tmdb): Flow<TvShow.Tmdb?> {
        return tvStorage.get(key)
    }

    override suspend fun insertTvShow(tv: TvShow.Tmdb) {
        tvStorage.put(tv.key, tv)
    }

    override fun getSeason(key: SeasonKey.Tmdb): Flow<SeasonWithEpisodes.Tmdb?> {
        return seasonStorage.get(key)
    }

    override suspend fun insertSeason(season: SeasonWithEpisodes.Tmdb) {
        seasonStorage.put(season.season.key, season)
    }

    override fun getMovie(key: MovieKey.Tmdb): Flow<TulipMovie.Tmdb?> {
        return movieStorage.get(key)
    }

    override suspend fun insertMovie(movie: TulipMovie.Tmdb) {
        movieStorage.put(movie.key, movie)
    }
}