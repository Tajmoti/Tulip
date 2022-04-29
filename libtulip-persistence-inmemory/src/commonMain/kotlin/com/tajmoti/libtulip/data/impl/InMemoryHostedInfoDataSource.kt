package com.tajmoti.libtulip.data.impl

import com.tajmoti.libtulip.data.HostedInfoDataSource
import com.tajmoti.libtulip.model.info.*
import com.tajmoti.libtulip.model.key.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class InMemoryHostedInfoDataSource : HostedInfoDataSource {
    private val tvShows = MutableStateFlow(setOf<TvShow.Hosted>())
    private val seasons = MutableStateFlow(setOf<SeasonWithEpisodes.Hosted>())
    private val movies = MutableStateFlow(setOf<TulipMovie.Hosted>())
    private val tmdbTvShowMappings = MutableStateFlow(mapOf<TvShowKey.Tmdb, Set<TvShowKey.Hosted>>())
    private val tmdbMovieMappings = MutableStateFlow(mapOf<MovieKey.Tmdb, Set<MovieKey.Hosted>>())

    override fun getTvShowByKey(key: TvShowKey.Hosted): Flow<TvShow.Hosted?> {
        return tvShows.map { tvShows -> tvShows.firstOrNull { tvShow -> tvShow.key == key } }
    }

    override fun getSeasonByKey(key: SeasonKey.Hosted): Flow<SeasonWithEpisodes.Hosted?> {
        return seasons.map { seasons -> seasons.firstOrNull { season -> season.season.key == key } }
    }

    override fun getMovieByKey(key: MovieKey.Hosted): Flow<TulipMovie.Hosted?> {
        return movies.map { movies -> movies.firstOrNull { movie -> movie.key == key } }
    }

    override suspend fun insertMovie(movie: TulipMovie.Hosted) {
        movies.value = movies.value.plus(movie)
    }

    override suspend fun insertTvShow(show: TvShow.Hosted) {
        tvShows.value = tvShows.value.plus(show)
    }

    override suspend fun insertSeasons(seasonss: List<SeasonWithEpisodes.Hosted>) {
        seasons.value = (seasons.value + seasonss)
    }

    override suspend fun createTmdbTvMapping(hosted: TvShowKey.Hosted, tmdb: TvShowKey.Tmdb) {
        val oldValue = tmdbTvShowMappings.value
        val oldSet = oldValue[tmdb] ?: mutableSetOf()
        val newSet = oldSet.plus(hosted)
        tmdbTvShowMappings.value = oldValue.plus(tmdb to newSet)
    }

    override suspend fun createTmdbMovieMapping(hosted: MovieKey.Hosted, tmdb: MovieKey.Tmdb) {
        val oldValue = tmdbMovieMappings.value
        val oldSet = oldValue[tmdb] ?: mutableSetOf()
        val newSet = oldSet.plus(hosted)
        tmdbMovieMappings.value = oldValue.plus(tmdb to newSet)
    }

    override fun getTmdbMappingForTvShow(tmdb: TvShowKey.Tmdb): Flow<Set<TvShowKey.Hosted>> {
        return tmdbTvShowMappings.map { keyMap ->
            keyMap.filterKeys { tmdbKey -> tmdbKey == tmdb }
                .flatMap { (_, hostedKeys) -> hostedKeys }
                .toSet()
        }
    }

    override fun getTmdbMappingForMovie(tmdb: MovieKey.Tmdb): Flow<Set<MovieKey.Hosted>> {
        return tmdbMovieMappings.map { keyMap ->
            keyMap.filterKeys { tmdbKey -> tmdbKey == tmdb }
                .flatMap { (_, hostedKeys) -> hostedKeys }
                .toSet()
        }
    }
}