package com.tajmoti.libtulip.data.impl

import com.tajmoti.libtulip.data.LocalTvDataSource
import com.tajmoti.libtulip.model.info.SeasonWithEpisodes
import com.tajmoti.libtulip.model.info.TulipMovie
import com.tajmoti.libtulip.model.info.TvShow
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.model.key.TvShowKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class InMemoryLocalTvDataSource : LocalTvDataSource {
    private val tvShows = MutableStateFlow<Set<TvShow.Tmdb>>(emptySet())
    private val seasons = MutableStateFlow<Set<SeasonWithEpisodes.Tmdb>>(emptySet())
    private val movies = MutableStateFlow<Set<TulipMovie.Tmdb>>(emptySet())

    override fun getTvShow(key: TvShowKey.Tmdb): Flow<TvShow.Tmdb?> {
        return tvShows.map { it.firstOrNull { tvShow -> tvShow.key == key } }
    }

    override suspend fun insertTvShow(tv: TvShow.Tmdb) {
        tvShows.value = tvShows.value.plus(tv)
    }

    override fun getSeason(key: SeasonKey.Tmdb): Flow<SeasonWithEpisodes.Tmdb?> {
        return seasons.map { it.firstOrNull { season -> season.season.key == key } }
    }

    override suspend fun insertSeason(season: SeasonWithEpisodes.Tmdb) {
        seasons.value = seasons.value.plus(season)
    }

    override fun getMovie(key: MovieKey.Tmdb): Flow<TulipMovie.Tmdb?> {
        return movies.map { movies -> movies.firstOrNull { movie -> movie.key == key } }
    }

    override suspend fun insertMovie(movie: TulipMovie.Tmdb) {
        movies.value = movies.value.plus(movie)
    }
}