package com.tajmoti.libtulip.data.impl

import com.tajmoti.libtulip.data.LocalTvDataSource
import com.tajmoti.libtulip.model.info.TulipEpisodeInfo
import com.tajmoti.libtulip.model.info.TulipMovie
import com.tajmoti.libtulip.model.info.TulipSeasonInfo
import com.tajmoti.libtulip.model.info.TulipTvShowInfo
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.model.key.TvShowKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class InMemoryLocalTvDataSource : LocalTvDataSource {
    private val tvShows = MutableStateFlow<Set<TulipTvShowInfo.Tmdb>>(emptySet())
    private val movies = MutableStateFlow<Set<TulipMovie.Tmdb>>(emptySet())

    override fun getTvShow(key: TvShowKey.Tmdb): Flow<TulipTvShowInfo.Tmdb?> {
        return tvShows.map { it.firstOrNull { tvShow -> tvShow.key == key } }
    }

    override fun getSeason(key: SeasonKey.Tmdb): Flow<TulipSeasonInfo.Tmdb?> {
        return getSeasons(key.tvShowKey).map { it.firstOrNull { season -> season.key == key } }
    }

    override fun getSeasons(key: TvShowKey.Tmdb): Flow<List<TulipSeasonInfo.Tmdb>> {
        return getTvShow(key).map { it?.seasons ?: emptyList() }
    }

    override fun getEpisode(key: EpisodeKey.Tmdb): Flow<TulipEpisodeInfo.Tmdb?> {
        return getEpisodes(key.seasonKey).map { it.firstOrNull { episode -> episode.key == key } }
    }

    override fun getEpisodes(key: SeasonKey.Tmdb): Flow<List<TulipEpisodeInfo.Tmdb>> {
        return getSeason(key).map { season -> season?.episodes ?: emptyList() }
    }

    override fun getMovie(key: MovieKey.Tmdb): Flow<TulipMovie.Tmdb?> {
        return movies.map { movies -> movies.firstOrNull { movie -> movie.key == key } }
    }

    override suspend fun insertTvShow(tv: TulipTvShowInfo.Tmdb) {
        tvShows.value = tvShows.value.plus(tv)
    }

    override suspend fun insertMovie(movie: TulipMovie.Tmdb) {
        movies.value = movies.value.plus(movie)
    }
}