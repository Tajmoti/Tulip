package com.tajmoti.tulip.adapter

import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.tulip.dao.ItemMappingDao
import com.tajmoti.tulip.entity.ItemMapping
import kotlinx.coroutines.flow.Flow

class MovieMappingDbAdapter : DbAdapter<ItemMappingDao, MovieKey.Hosted, ItemMapping> {

    override fun findByKeyFromDb(dao: ItemMappingDao, key: MovieKey.Hosted): Flow<ItemMapping?> {
        return dao.getTmdbIdByHostedKey(key.streamingService, key.id)
    }

    fun findByKeyFromByTmdbKey(dao: ItemMappingDao, key: MovieKey.Tmdb): Flow<List<ItemMapping>> {
        return dao.getHostedKeysByTmdbId(key.id)
    }

    override suspend fun insertToDb(dao: ItemMappingDao, entity: ItemMapping) {
        dao.insert(entity)
    }

    override suspend fun insertToDb(dao: ItemMappingDao, entities: List<ItemMapping>) {
        dao.insert(entities)
    }
}