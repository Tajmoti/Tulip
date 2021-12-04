package com.tajmoti.tulip.datasource

import com.tajmoti.libtulip.data.LocalTvDataSource
import com.tajmoti.libtulip.model.info.TulipEpisodeInfo
import com.tajmoti.libtulip.model.info.TulipMovie
import com.tajmoti.libtulip.model.info.TulipSeasonInfo
import com.tajmoti.libtulip.model.info.TulipTvShowInfo
import com.tajmoti.libtulip.model.key.*
import com.tajmoti.tulip.db.dao.tmdb.TmdbDao
import com.tajmoti.tulip.db.entity.tmdb.DbTmdbSeason
import javax.inject.Inject

class AndroidTvDataSource @Inject constructor(
    private val dao: TmdbDao
) : LocalTvDataSource {

    override suspend fun getTvShow(key: TvShowKey.Tmdb): TulipTvShowInfo.Tmdb? {
        return dao.getTv(key.id)?.let { tv ->
            val seasons = getSeasons(key).map { season -> season }
            tv.fromDb(key, seasons)
        }
    }

    private suspend fun insertTv(tv: TulipTvShowInfo.Tmdb) {
        dao.insertTv(tv.toDb())
    }

    override suspend fun insertTvShow(tv: TulipTvShowInfo.Tmdb) {
        insertTv(tv)
        insertSeasons(tv.key, tv.seasons)
    }

    override suspend fun getSeason(key: SeasonKey.Tmdb): TulipSeasonInfo.Tmdb? {
        return dao.getSeason(key.tvShowKey.id, key.seasonNumber)
            ?.let { getSeasonWithEpisodes(key, it) }
    }

    private suspend fun getSeasonWithEpisodes(
        seasonKey: SeasonKey.Tmdb,
        dbSeason: DbTmdbSeason
    ): TulipSeasonInfo.Tmdb {
        val episodes = getEpisodes(seasonKey)
        return dbSeason.fromDb(seasonKey, episodes)
    }

    override suspend fun getSeasons(key: TvShowKey.Tmdb): List<TulipSeasonInfo.Tmdb> {
        return dao.getSeasons(key.id)
            .map {
                val seasonKey = SeasonKey.Tmdb(key, it.seasonNumber)
                getSeasonWithEpisodes(seasonKey, it)
            }
    }

    private suspend inline fun insertSeasons(
        tvId: TvShowKey.Tmdb,
        seasons: List<TulipSeasonInfo.Tmdb>
    ) {
        val dbSeasons = seasons.map { season -> season.toDb(tvId.id) }
        dao.insertSeasons(dbSeasons)
        val episodes = seasons.flatMap { it.episodes }
        insertEpisodes(tvId.id, episodes)
    }

    override suspend fun getEpisode(key: EpisodeKey.Tmdb): TulipEpisodeInfo.Tmdb? {
        return dao.getEpisode(key.tvShowKey.id, key.seasonNumber, key.episodeNumber)?.fromDb(key)
    }

    override suspend fun getEpisodes(key: SeasonKey.Tmdb): List<TulipEpisodeInfo.Tmdb> {
        return dao.getEpisodes(key.tvShowKey.id, key.seasonNumber).map { it.fromDb(key) }
    }

    private suspend inline fun insertEpisodes(tvId: Long, episodes: List<TulipEpisodeInfo.Tmdb>) {
        val dbEpisodes = episodes.map { it.toDb(tvId) }
        dao.insertEpisodes(dbEpisodes)
    }

    override suspend fun getMovie(key: MovieKey.Tmdb): TulipMovie.Tmdb? {
        return dao.getMovie(key.id)?.fromDb()
    }

    override suspend fun insertMovie(movie: TulipMovie.Tmdb) {
        dao.insertMovie(movie.toDb())
    }
}