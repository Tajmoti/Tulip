package com.tajmoti.tulip.datasource

import com.tajmoti.libtulip.data.HostedInfoDataSource
import com.tajmoti.libtulip.model.info.*
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.model.key.streamingService
import com.tajmoti.tulip.db.dao.hosted.*
import com.tajmoti.tulip.db.entity.hosted.DbTmdbMapping
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class AndroidHostedInfoDataSource @Inject constructor(
    private val tvShowDao: TvShowDao,
    private val seasonDao: SeasonDao,
    private val episodeDao: EpisodeDao,
    private val movieDao: MovieDao,
    private val tmdbMappingDao: TmdbMappingDao
) : HostedInfoDataSource {

    override fun getTvShowByKey(key: TvShowKey.Hosted): Flow<TvShow.Hosted?> {
        val tmdbIdFlow = tmdbMappingDao.getTmdbIdByHostedKey(key.streamingService, key.id)
        val itemFlow = tvShowDao.getByKey(key.streamingService, key.id)
        val seasonsFlow = getSeasonsByTvShow(key)
        return combine(tmdbIdFlow, itemFlow, seasonsFlow) { tmdbId, item, seasons ->
            item?.fromDb(key, tmdbId?.tmdbId, seasons)
        }
    }

    private fun getSeasonsByTvShow(key: TvShowKey.Hosted): Flow<List<Season.Hosted>> {
        return seasonDao.getForShow(key.streamingService, key.id)
            .map { seasons -> seasons.map { season -> Season.Hosted(SeasonKey.Hosted(key, season.number), season.number) } }
    }

    private fun getEpisodesBySeason(key: SeasonKey.Hosted): Flow<List<Episode.Hosted>> {
        return episodeDao.getForSeason(key.streamingService, key.tvShowKey.id, key.seasonNumber)
            .map { episodes -> episodes.map { episode -> episode.fromDb(key) } }
    }

    override suspend fun insertTvShow(show: TvShow.Hosted) {
        tvShowDao.insert(show.toDb(show))
        seasonDao.insert(show.seasons.map { it.toDb() })
    }


    override fun getSeasonByKey(key: SeasonKey.Hosted): Flow<SeasonWithEpisodes.Hosted?> {
        return seasonDao.getBySeasonNumber(key.streamingService, key.tvShowKey.id, key.seasonNumber)
            .map { it?.fromDbWithEpisodes(key.tvShowKey, getEpisodesBySeason(key).first()) } // TODO Properly fix .first()
    }

    override suspend fun insertSeasons(seasons: List<SeasonWithEpisodes.Hosted>) {
        seasonDao.insert(seasons.map { it.season.toDb() })
        episodeDao.insert(seasons.flatMap { it.episodes }.map { it.toDb() })
    }


    override fun getMovieByKey(key: MovieKey.Hosted): Flow<TulipMovie.Hosted?> {
        val tmdbIdFlow = tmdbMappingDao.getTmdbIdByHostedKey(key.streamingService, key.id)
        val movieFlow = movieDao.getByKey(key.streamingService, key.id)
        return combine(tmdbIdFlow, movieFlow) { tmdbId, movie ->
            movie?.fromDb(key, tmdbId?.tmdbId)
        }
    }

    override suspend fun insertMovie(movie: TulipMovie.Hosted) {
        movieDao.insert(movie.toDb(movie.info))
    }

    override suspend fun createTmdbTvMapping(hosted: TvShowKey.Hosted, tmdb: TvShowKey.Tmdb) {
        tmdbMappingDao.insert(DbTmdbMapping(hosted.streamingService, hosted.id, tmdb.id))
    }

    override suspend fun createTmdbMovieMapping(hosted: MovieKey.Hosted, tmdb: MovieKey.Tmdb) {
        tmdbMappingDao.insert(DbTmdbMapping(hosted.streamingService, hosted.id, tmdb.id))
    }

    override fun getTmdbMappingForTvShow(tmdb: TvShowKey.Tmdb): Flow<Set<TvShowKey.Hosted>> {
        return tmdbMappingDao.getHostedKeysByTmdbId(tmdb.id)
            .map { mappings ->
                mappings
                    .map { mapping -> TvShowKey.Hosted(mapping.service, mapping.key) }
                    .toSet()
            }
    }

    override fun getTmdbMappingForMovie(tmdb: MovieKey.Tmdb): Flow<Set<MovieKey.Hosted>> {
        return tmdbMappingDao.getHostedKeysByTmdbId(tmdb.id)
            .map { mappings ->
                mappings
                    .map { mapping -> MovieKey.Hosted(mapping.service, mapping.key) }
                    .toSet()
            }
    }
}