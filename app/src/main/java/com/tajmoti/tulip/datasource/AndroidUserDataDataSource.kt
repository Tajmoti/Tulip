package com.tajmoti.tulip.datasource

import com.tajmoti.libtulip.data.UserDataDataSource
import com.tajmoti.libtulip.model.key.ItemKey
import com.tajmoti.tulip.db.dao.userdata.FavoritesDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AndroidUserDataDataSource @Inject constructor(
    private val userDataDao: FavoritesDao
) : UserDataDataSource {

    override suspend fun getUserFavorites(): List<ItemKey> {
        val tmdbItems = userDataDao.getAllTmdbFavorites().map { it.fromDb() }
        val hostedItems = userDataDao.getAllHostedFavorites().map { it.fromDb() }
        return tmdbItems + hostedItems
    }

    override fun getUserFavoritesAsFlow(): Flow<List<ItemKey>> {
        val tmdbItems = userDataDao.getAllTmdbFavoritesAsFlow()
            .map { it.map { item -> item.fromDb() } }
        val hostedItems = userDataDao.getAllHostedFavoritesAsFlow()
            .map { it.map { item -> item.fromDb() } }
        return combine(tmdbItems, hostedItems) { a, b -> a + b }
    }

    override suspend fun deleteUserFavorite(item: ItemKey) {
        when (item) {
            is ItemKey.Tmdb -> userDataDao.deleteTmdbFavorite(item.toDb())
            is ItemKey.Hosted -> userDataDao.deleteHostedFavorite(item.toDb())
        }
    }

    override suspend fun addUserFavorite(item: ItemKey) {
        when (item) {
            is ItemKey.Tmdb -> userDataDao.insertTmdbFavorite(item.toDb())
            is ItemKey.Hosted -> userDataDao.insertHostedFavorite(item.toDb())
        }
    }
}