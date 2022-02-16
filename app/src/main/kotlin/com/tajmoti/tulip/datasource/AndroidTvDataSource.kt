package com.tajmoti.tulip.datasource

import com.tajmoti.commonutils.combineNonEmpty
import com.tajmoti.libtulip.data.LocalTvDataSource
import com.tajmoti.libtulip.model.info.TulipEpisodeInfo
import com.tajmoti.libtulip.model.info.TulipMovie
import com.tajmoti.libtulip.model.info.TulipSeasonInfo
import com.tajmoti.libtulip.model.info.TulipTvShowInfo
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.model.key.tvShowKey
import com.tajmoti.tulip.db.dao.tmdb.TmdbDao
import com.tajmoti.tulip.db.entity.tmdb.DbTmdbSeason
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flattenConcat
import kotlinx.coroutines.flow.map
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

    private fun getSeasonWithEpisodes(key: SeasonKey.Tmdb, dbSeason: DbTmdbSeason): Flow<TulipSeasonInfo.Tmdb> {
        return getEpisodes(key).map { dbSeason.fromDb(key, it) }
    }

    private fun getSeasons(key: TvShowKey.Tmdb): Flow<List<TulipSeasonInfo.Tmdb>> {
        return dao.getSeasons(key.id)
            .map(::getEpisodesForSeasons)
            .flattenConcat()
    }

    private fun getEpisodesForSeasons(seasons: List<DbTmdbSeason>): Flow<List<TulipSeasonInfo.Tmdb>> {
        return seasons.map(::getEpisodesForSeason).combineNonEmpty()
    }

    private fun getEpisodesForSeason(season: DbTmdbSeason): Flow<TulipSeasonInfo.Tmdb> {
        val seasonKey = SeasonKey.Tmdb(TvShowKey.Tmdb(season.tvId), season.seasonNumber)
        return getSeasonWithEpisodes(seasonKey, season)
    }

    private fun getEpisodes(key: SeasonKey.Tmdb): Flow<List<TulipEpisodeInfo.Tmdb>> {
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