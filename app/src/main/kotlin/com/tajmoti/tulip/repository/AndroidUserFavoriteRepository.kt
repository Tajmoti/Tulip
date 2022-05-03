package com.tajmoti.tulip.repository

import com.tajmoti.libtulip.repository.UserFavoriteRepository
import com.tajmoti.libtulip.model.info.ItemType
import com.tajmoti.libtulip.model.key.ItemKey
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.tulip.db.dao.userdata.FavoritesDao
import com.tajmoti.tulip.db.entity.userdata.DbFavoriteHostedItem
import com.tajmoti.tulip.db.entity.userdata.DbFavoriteTmdbItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AndroidUserFavoriteRepository @Inject constructor(
    private val favoritesDao: FavoritesDao
) : UserFavoriteRepository {

    override fun isFavorite(item: ItemKey): Flow<Boolean> {
        return when (item) {
            is MovieKey.Hosted -> favoritesDao.isHostedFavorite(ItemType.MOVIE, item.streamingService, item.id)
            is TvShowKey.Hosted -> favoritesDao.isHostedFavorite(ItemType.TV_SHOW, item.streamingService, item.id)
            is MovieKey.Tmdb -> favoritesDao.isTmdbFavorite(ItemType.MOVIE, item.id)
            is TvShowKey.Tmdb -> favoritesDao.isTmdbFavorite(ItemType.TV_SHOW, item.id)
        }
    }

    override fun getUserFavorites(): Flow<Set<ItemKey>> {
        val tmdbItems = favoritesDao.getAllTmdbFavorites()
            .map { it.map { item -> item.fromDb() } }
        val hostedItems = favoritesDao.getAllHostedFavorites()
            .map { it.map { item -> item.fromDb() } }
        return combine(tmdbItems, hostedItems) { a, b -> (a + b).toSet() }
    }

    internal fun DbFavoriteTmdbItem.fromDb(): ItemKey {
        return when (type) {
            ItemType.TV_SHOW -> TvShowKey.Tmdb(tmdbItemId)
            ItemType.MOVIE -> MovieKey.Tmdb(tmdbItemId)
        }
    }

    internal fun DbFavoriteHostedItem.fromDb(): ItemKey {
        return when (type) {
            ItemType.TV_SHOW -> TvShowKey.Hosted(streamingService, key)
            ItemType.MOVIE -> MovieKey.Hosted(streamingService, key)
        }
    }

    internal fun ItemKey.Tmdb.toDb(): DbFavoriteTmdbItem {
        val type = when (this) {
            is TvShowKey.Tmdb -> ItemType.TV_SHOW
            is MovieKey.Tmdb -> ItemType.MOVIE
        }
        return DbFavoriteTmdbItem(type, id)
    }

    override suspend fun deleteUserFavorite(item: ItemKey) {
        when (item) {
            is ItemKey.Tmdb -> favoritesDao.deleteTmdbFavorite(item.toDb())
            is ItemKey.Hosted -> favoritesDao.deleteHostedFavorite(item.toDb())
        }
    }

    override suspend fun addUserFavorite(item: ItemKey) {
        when (item) {
            is ItemKey.Tmdb -> favoritesDao.insertTmdbFavorite(item.toDb())
            is ItemKey.Hosted -> favoritesDao.insertHostedFavorite(item.toDb())
        }
    }

    fun ItemKey.Hosted.toDb(): DbFavoriteHostedItem {
        val type = when (this) {
            is TvShowKey.Hosted -> ItemType.TV_SHOW
            is MovieKey.Hosted -> ItemType.MOVIE
        }
        return DbFavoriteHostedItem(type, streamingService, id)
    }
}