package com.tajmoti.tulip.adapter

import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.tulip.dao.ItemMappingDao
import com.tajmoti.tulip.entity.ItemMapping
import kotlinx.coroutines.flow.Flow

class TvShowMappingDbAdapter : DbAdapter<ItemMappingDao, TvShowKey.Hosted, ItemMapping> {

    override fun findByKeyFromDb(dao: ItemMappingDao, key: TvShowKey.Hosted): Flow<ItemMapping?> {
        return dao.getTmdbIdByHostedKey(key.streamingService, key.id)
    }

    fun findByKeyFromByTmdbKey(dao: ItemMappingDao, key: TvShowKey.Tmdb): Flow<List<ItemMapping>> {
        return dao.getHostedKeysByTmdbId(key.id)
    }

    override suspend fun insertToDb(dao: ItemMappingDao, entity: ItemMapping) {
        dao.insert(entity)
    }

    override suspend fun insertToDb(dao: ItemMappingDao, entities: List<ItemMapping>) {
        dao.insert(entities)
    }
}