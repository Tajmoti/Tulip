package com.tajmoti.tulip.datasource

import com.tajmoti.commonutils.mapNotNulls
import com.tajmoti.libtulip.data.LocalTvDataSource
import com.tajmoti.libtulip.model.info.Episode
import com.tajmoti.libtulip.model.info.SeasonWithEpisodes
import com.tajmoti.libtulip.model.info.TulipMovie
import com.tajmoti.libtulip.model.info.TvShow
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.tulip.db.dao.tmdb.TmdbDao
import com.tajmoti.tulip.db.entity.tmdb.DbTmdbSeason
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class AndroidTvDataSource @Inject constructor(
    private val dao: TmdbDao
) : LocalTvDataSource {

    override fun getTvShow(key: TvShowKey.Tmdb): Flow<TvShow.Tmdb?> {
        return dao.getTv(key.id).map { it?.fromDb(key) }
    }

    override suspend fun insertTvShow(tv: TvShow.Tmdb) {
        dao.insertTv(tv.toDb())
        dao.insertSeasons(tv.seasons.map { it.toDb() })
    }

    override fun getSeason(key: SeasonKey.Tmdb): Flow<SeasonWithEpisodes.Tmdb?> {
        return dao.getSeason(key.tvShowKey.id, key.seasonNumber)
            .mapNotNulls { season -> getEpisodesForSeason(season) }
            .flatMapLatest { it ?: flowOf(null) }
    }

    private fun getEpisodesForSeason(season: DbTmdbSeason): Flow<SeasonWithEpisodes.Tmdb> {
        val seasonKey = SeasonKey.Tmdb(TvShowKey.Tmdb(season.tvId), season.seasonNumber)
        return getSeasonWithEpisodes(seasonKey, season)
    }
    private fun getSeasonWithEpisodes(key: SeasonKey.Tmdb, dbSeason: DbTmdbSeason): Flow<SeasonWithEpisodes.Tmdb> {
        return getEpisodes(key).map { dbSeason.fromDbWithEpisodes(key, it) }
    }

    override suspend fun insertSeason(season: SeasonWithEpisodes.Tmdb) {
        dao.insertSeason(season.season.toDb())
        dao.insertEpisodes(season.episodes.map { it.toDb(season.season.key.tvShowKey.id) })
    }

    private fun getEpisodes(key: SeasonKey.Tmdb): Flow<List<Episode.Tmdb>> {
        return dao.getEpisodes(key.tvShowKey.id, key.seasonNumber)
            .map { it.map { dbEpisode -> dbEpisode.fromDb(key) } }
    }

    override fun getMovie(key: MovieKey.Tmdb): Flow<TulipMovie.Tmdb?> {
        return dao.getMovie(key.id)
            .map { it?.fromDb() }
    }

    override suspend fun insertMovie(movie: TulipMovie.Tmdb) {
        dao.insertMovie(movie.toDb())
    }
}