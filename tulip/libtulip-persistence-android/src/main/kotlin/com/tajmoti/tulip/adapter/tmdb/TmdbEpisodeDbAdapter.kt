package com.tajmoti.tulip.adapter.tmdb

import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.model.key.seasonNumber
import com.tajmoti.libtulip.model.key.tvShowKey
import com.tajmoti.tulip.adapter.DbAdapter
import com.tajmoti.tulip.dao.tmdb.TmdbEpisodeDao
import com.tajmoti.tulip.entity.tmdb.TmdbEpisode
import kotlinx.coroutines.flow.Flow

class TmdbEpisodeDbAdapter : DbAdapter<TmdbEpisodeDao, EpisodeKey.Tmdb, TmdbEpisode> {

    override fun findByKeyFromDb(dao: TmdbEpisodeDao, key: EpisodeKey.Tmdb): Flow<TmdbEpisode?> {
        return dao.getEpisode(key.tvShowKey.id, key.seasonNumber, key.episodeNumber)
    }

    fun findBySeasonKeyFromDb(dao: TmdbEpisodeDao, key: SeasonKey.Tmdb): Flow<List<TmdbEpisode>> = with(key) {
        return dao.getEpisodes(tvShowKey.id, seasonNumber)
    }

    override suspend fun insertToDb(dao: TmdbEpisodeDao, entity: TmdbEpisode) {
        dao.insertEpisode(entity)
    }

    override suspend fun insertToDb(dao: TmdbEpisodeDao, entities: List<TmdbEpisode>) {
        dao.insertEpisodes(entities)
    }
}