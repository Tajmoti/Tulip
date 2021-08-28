package com.tajmoti.tulip.repository.impl

import com.tajmoti.libtulip.data.UserDataDataSource
import com.tajmoti.libtulip.model.key.ItemKey
import com.tajmoti.libtulip.model.userdata.UserFavorite
import com.tajmoti.tulip.db.dao.userdata.FavoritesDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AndroidUserDataDataSource @Inject constructor(
    private val userDataDao: FavoritesDao
) : UserDataDataSource {

    override suspend fun getUserFavorites(): List<UserFavorite> {
        val tmdbItems = userDataDao.getAllTmdbFavorites().map { it.fromDb() }
        val hostedItems = userDataDao.getAllHostedFavorites().map { it.fromDb() }
        return tmdbItems + hostedItems
    }

    override fun getUserFavoritesAsFlow(): Flow<List<UserFavorite>> {
        val tmdbItems = userDataDao.getAllTmdbFavoritesAsFlow()
            .map { it.map { item -> item.fromDb() } }
        val hostedItems = userDataDao.getAllHostedFavoritesAsFlow()
            .map { it.map { item -> item.fromDb() } }
        return combine(tmdbItems, hostedItems) { a, b -> a + b }
    }

    override suspend fun deleteUserFavorite(item: UserFavorite) {
        when (val info = item.info) {
            is ItemKey.Tmdb -> userDataDao.deleteTmdbFavorite(info.toDb())
            is ItemKey.Hosted -> userDataDao.deleteHostedFavorite(info.toDb())
        }
    }

    override suspend fun addUserFavorite(item: UserFavorite) {
        when (val info = item.info) {
            is ItemKey.Tmdb -> userDataDao.insertTmdbFavorite(info.toDb())
            is ItemKey.Hosted -> userDataDao.insertHostedFavorite(info.toDb())
        }
    }
}