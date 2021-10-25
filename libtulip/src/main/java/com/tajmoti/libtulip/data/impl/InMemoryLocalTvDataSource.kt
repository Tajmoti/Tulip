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

class InMemoryLocalTvDataSource : LocalTvDataSource {
    private val tvShows = mutableSetOf<TulipTvShowInfo.Tmdb>()
    private val movies = mutableSetOf<TulipMovie.Tmdb>()

    override suspend fun getTvShow(key: TvShowKey.Tmdb): TulipTvShowInfo.Tmdb? {
        return tvShows.firstOrNull { it.key == key }
    }

    override suspend fun getSeason(key: SeasonKey.Tmdb): TulipSeasonInfo.Tmdb? {
        return getSeasons(key.tvShowKey).firstOrNull { it.key == key }
    }

    override suspend fun getSeasons(key: TvShowKey.Tmdb): List<TulipSeasonInfo.Tmdb> {
        return getTvShow(key)?.seasons ?: emptyList()
    }

    override suspend fun getEpisode(key: EpisodeKey.Tmdb): TulipEpisodeInfo.Tmdb? {
        return getEpisodes(key.seasonKey).firstOrNull { it.key == key }
    }

    override suspend fun getEpisodes(key: SeasonKey.Tmdb): List<TulipEpisodeInfo.Tmdb> {
        return getSeason(key)?.episodes ?: emptyList()
    }

    override suspend fun getMovie(key: MovieKey.Tmdb): TulipMovie.Tmdb? {
        return movies.firstOrNull { it.key == key }
    }

    override suspend fun insertTvShow(tv: TulipTvShowInfo.Tmdb) {
        tvShows.add(tv)
    }

    override suspend fun insertMovie(movie: TulipMovie.Tmdb) {
        movies.add(movie)
    }
}