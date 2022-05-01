package com.tajmoti.tulip.db.adapter

import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.model.key.seasonNumber
import com.tajmoti.libtulip.model.key.tvShowKey
import com.tajmoti.tulip.db.dao.tmdb.TmdbDao
import com.tajmoti.tulip.db.entity.tmdb.DbTmdbEpisode
import kotlinx.coroutines.flow.Flow

class TmdbEpisodeDbAdapter : DbAdapter<TmdbDao, EpisodeKey.Tmdb, DbTmdbEpisode> {

    override fun findByKeyFromDb(dao: TmdbDao, key: EpisodeKey.Tmdb): Flow<DbTmdbEpisode?> {
        return dao.getEpisode(key.tvShowKey.id, key.seasonNumber, key.episodeNumber)
    }

    fun findBySeasonKeyFromDb(dao: TmdbDao, key: SeasonKey.Tmdb): Flow<List<DbTmdbEpisode>> = with(key) {
        return dao.getEpisodes(tvShowKey.id, seasonNumber)
    }

    override suspend fun insertToDb(dao: TmdbDao, entity: DbTmdbEpisode) {
        dao.insertEpisode(entity)
    }

    override suspend fun insertToDb(dao: TmdbDao, entities: List<DbTmdbEpisode>) {
        dao.insertEpisodes(entities)
    }
}