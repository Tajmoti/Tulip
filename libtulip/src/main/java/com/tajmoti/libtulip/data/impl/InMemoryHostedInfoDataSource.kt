package com.tajmoti.libtulip.data.impl

import com.tajmoti.libtulip.data.HostedInfoDataSource
import com.tajmoti.libtulip.model.info.TulipEpisodeInfo
import com.tajmoti.libtulip.model.info.TulipMovie
import com.tajmoti.libtulip.model.info.TulipSeasonInfo
import com.tajmoti.libtulip.model.info.TulipTvShowInfo
import com.tajmoti.libtulip.model.key.*
import com.tajmoti.libtulip.model.tmdb.TmdbItemId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class InMemoryHostedInfoDataSource : HostedInfoDataSource {
    private val tvShows = mutableSetOf<TulipTvShowInfo.Hosted>()
    private val movies = mutableSetOf<TulipMovie.Hosted>()
    private val tmdbMappings = mutableMapOf<TmdbItemId, MutableSet<ItemKey.Hosted>>()

    override suspend fun getTvShowByKey(key: TvShowKey.Hosted): TulipTvShowInfo.Hosted? {
        return tvShows.firstOrNull { it.key == key }
    }

    override suspend fun getTvShowsByTmdbId(key: TvShowKey.Tmdb): List<TulipTvShowInfo.Hosted> {
        val showKeys = tmdbMappings[key.id] ?: return emptyList()
        return tvShows.filter { showKeys.contains(it.key) }
    }

    override suspend fun insertTvShow(show: TulipTvShowInfo.Hosted) {
        tvShows.add(show)
    }

    override suspend fun getSeasonsByTvShow(key: TvShowKey.Hosted): List<TulipSeasonInfo.Hosted> {
        return tvShows.firstOrNull { it.key == key }?.seasons ?: emptyList()
    }

    override suspend fun getSeasonByKey(key: SeasonKey.Hosted): TulipSeasonInfo.Hosted? {
        return getSeasonsByTvShow(key.tvShowKey).firstOrNull { it.key == key }
    }

    override suspend fun getEpisodesBySeason(key: SeasonKey.Hosted): List<TulipEpisodeInfo.Hosted> {
        return getSeasonByKey(key)?.episodes ?: emptyList()
    }

    override suspend fun getEpisodeByKey(key: EpisodeKey.Hosted): TulipEpisodeInfo.Hosted? {
        return getSeasonByKey(key.seasonKey)?.episodes?.firstOrNull { it.key == key }
    }

    override suspend fun getEpisodeByTmdbId(key: EpisodeKey.Tmdb): List<TulipEpisodeInfo.Hosted> {
        val seasons = getTvShowsByTmdbId(key.tvShowKey).mapNotNull { tvShow ->
            tvShow.seasons.firstOrNull { season -> season.key.seasonNumber == key.seasonNumber }
        }
        return seasons.mapNotNull { season ->
            season.episodes.firstOrNull { episode -> episode.episodeNumber == key.episodeNumber }
        }
    }

    override suspend fun getMovieByKey(key: MovieKey.Hosted): TulipMovie.Hosted? {
        return movies.firstOrNull { it.key == key }
    }

    override suspend fun getMovieByTmdbKey(key: MovieKey.Tmdb): List<TulipMovie.Hosted> {
        val showKeys = tmdbMappings[key.id] ?: return emptyList()
        return movies.filter { showKeys.contains(it.key) }
    }

    override suspend fun insertMovie(movie: TulipMovie.Hosted) {
        movies.add(movie)
    }

    override suspend fun createTmdbMapping(hosted: ItemKey.Hosted, tmdb: TmdbItemId) {
        val list = tmdbMappings[tmdb] ?: mutableSetOf()
        list.add(hosted)
        tmdbMappings[tmdb] = list
    }

    override fun getTmdbMappingForTvShow(tmdb: TmdbItemId.Tv): Flow<List<TvShowKey.Hosted>> {
        return flowOf(tmdbMappings[tmdb]?.map { it as TvShowKey.Hosted } ?: emptyList())
    }

    override fun getTmdbMappingForMovie(tmdb: TmdbItemId.Movie): Flow<List<MovieKey.Hosted>> {
        return flowOf(tmdbMappings[tmdb]?.map { it as MovieKey.Hosted } ?: emptyList())
    }
}