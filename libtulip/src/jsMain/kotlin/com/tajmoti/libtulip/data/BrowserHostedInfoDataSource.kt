package com.tajmoti.libtulip.data

import com.tajmoti.libtulip.model.info.TulipMovie
import com.tajmoti.libtulip.model.info.SeasonWithEpisodes
import com.tajmoti.libtulip.model.info.TvShow
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.model.key.TvShowKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BrowserHostedInfoDataSource : HostedInfoDataSource {
    private val tvShows = BrowserStorage<TvShowKey.Hosted, TvShow.Hosted>("1")
    private val seasons = BrowserStorage<SeasonKey.Hosted, SeasonWithEpisodes.Hosted>("5")
    private val movies = BrowserStorage<MovieKey.Hosted, TulipMovie.Hosted>("2")
    private val tmdbTvShowMappings = BrowserStorage<TvShowKey.Tmdb, Set<TvShowKey.Hosted>>("3")
    private val tmdbMovieMappings = BrowserStorage<MovieKey.Tmdb, Set<MovieKey.Hosted>>("4")

    override fun getTvShowByKey(key: TvShowKey.Hosted): Flow<TvShow.Hosted?> {
        return tvShows.get(key)
    }

    override suspend fun insertTvShow(show: TvShow.Hosted) {
        tvShows.put(show.key, show)
    }

    override fun getSeasonByKey(key: SeasonKey.Hosted): Flow<SeasonWithEpisodes.Hosted?> {
        return seasons.get(key)
    }

    override suspend fun insertSeasons(seasonss: List<SeasonWithEpisodes.Hosted>) {
        for (season in seasonss) {
            seasons.put(season.season.key, season)
        }
    }

    override fun getMovieByKey(key: MovieKey.Hosted): Flow<TulipMovie.Hosted?> {
        return movies.get(key)
    }

    override suspend fun insertMovie(movie: TulipMovie.Hosted) {
        movies.put(movie.key, movie)
    }

    override suspend fun createTmdbTvMapping(hosted: TvShowKey.Hosted, tmdb: TvShowKey.Tmdb) {
        tmdbTvShowMappings.update(tmdb) { oldValue -> (oldValue ?: mutableSetOf()).plus(hosted) }
    }

    override suspend fun createTmdbMovieMapping(hosted: MovieKey.Hosted, tmdb: MovieKey.Tmdb) {
        tmdbMovieMappings.update(tmdb) { oldValue -> (oldValue ?: mutableSetOf()).plus(hosted) }
    }

    override fun getTmdbMappingForTvShow(tmdb: TvShowKey.Tmdb): Flow<Set<TvShowKey.Hosted>> {
        return tmdbTvShowMappings.get(tmdb).map { it ?: emptySet() }
    }

    override fun getTmdbMappingForMovie(tmdb: MovieKey.Tmdb): Flow<Set<MovieKey.Hosted>> {
        return tmdbMovieMappings.get(tmdb).map { it ?: emptySet() }
    }
}