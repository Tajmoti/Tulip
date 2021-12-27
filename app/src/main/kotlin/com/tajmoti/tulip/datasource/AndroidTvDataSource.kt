package com.tajmoti.tulip.datasource

import com.tajmoti.commonutils.combineNonEmpty
import com.tajmoti.commonutils.mapNotNulls
import com.tajmoti.libtulip.data.LocalTvDataSource
import com.tajmoti.libtulip.model.info.TulipEpisodeInfo
import com.tajmoti.libtulip.model.info.TulipMovie
import com.tajmoti.libtulip.model.info.TulipSeasonInfo
import com.tajmoti.libtulip.model.info.TulipTvShowInfo
import com.tajmoti.libtulip.model.key.*
import com.tajmoti.tulip.db.dao.tmdb.TmdbDao
import com.tajmoti.tulip.db.entity.tmdb.DbTmdbSeason
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class AndroidTvDataSource @Inject constructor(
    private val dao: TmdbDao
) : LocalTvDataSource {

    override fun getTvShow(key: TvShowKey.Tmdb): Flow<TulipTvShowInfo.Tmdb?> {
        val tvFlow = dao.getTv(key.id)
        val seasonsFlow = getSeasons(key)
        return combine(tvFlow, seasonsFlow) { tv, seasons ->
            tv?.fromDb(key, seasons)
        }
    }

    override suspend fun insertTvShow(tv: TulipTvShowInfo.Tmdb) {
        dao.insertTv(tv.toDb())
        val dbSeasons = tv.seasons
            .map { season -> season.toDb(season.key.tvShowKey.id) }
        dao.insertSeasons(dbSeasons)
        val dbEpisodes = tv.seasons
            .flatMap { it.episodes }
            .map { it.toDb(it.key.tvShowKey.id) }
        dao.insertEpisodes(dbEpisodes)
    }

    override fun getSeason(key: SeasonKey.Tmdb): Flow<TulipSeasonInfo.Tmdb?> {
        return dao.getSeason(key.tvShowKey.id, key.seasonNumber)
            .flatMapLatest { it?.let { getSeasonWithEpisodes(key, it) } ?: flowOf(null) }
    }

    private fun getSeasonWithEpisodes(key: SeasonKey.Tmdb, dbSeason: DbTmdbSeason): Flow<TulipSeasonInfo.Tmdb> {
        return getEpisodes(key).map { dbSeason.fromDb(key, it) }
    }

    override fun getSeasons(key: TvShowKey.Tmdb): Flow<List<TulipSeasonInfo.Tmdb>> {
        return dao.getSeasons(key.id)
            .map { getEpForShow(it) }
            .flattenConcat()
    }

    private fun getEpForShow(seasons: List<DbTmdbSeason>): Flow<List<TulipSeasonInfo.Tmdb>> {
        val seasonFlows = seasons.map {
            val seasonKey = SeasonKey.Tmdb(TvShowKey.Tmdb(it.tvId), it.seasonNumber)
            getSeasonWithEpisodes(seasonKey, it)
        }
        return seasonFlows.combineNonEmpty()
    }

    override fun getEpisode(key: EpisodeKey.Tmdb): Flow<TulipEpisodeInfo.Tmdb?> {
        return dao.getEpisode(key.tvShowKey.id, key.seasonNumber, key.episodeNumber)
            .mapNotNulls { it.fromDb(key) }
    }

    override fun getEpisodes(key: SeasonKey.Tmdb): Flow<List<TulipEpisodeInfo.Tmdb>> {
        return dao.getEpisodes(key.tvShowKey.id, key.seasonNumber)
            .map { it.map { dbEpisode -> dbEpisode.fromDb(key) } }
    }

    override fun getMovie(key: MovieKey.Tmdb): Flow<TulipMovie.Tmdb?> {
        return dao.getMovie(key.id)
            .mapNotNulls { it.fromDb() }
    }

    override suspend fun insertMovie(movie: TulipMovie.Tmdb) {
        dao.insertMovie(movie.toDb())
    }
}