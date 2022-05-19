package com.tajmoti.tulip.repository.user

import com.tajmoti.libtulip.model.key.ItemKey
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.repository.UserFavoriteRepository
import com.tajmoti.tulip.dao.user.FavoriteDao
import com.tajmoti.tulip.entity.user.FavoriteHostedItem
import com.tajmoti.tulip.entity.user.FavoriteTmdbItem
import com.tajmoti.tulip.entity.user.ItemType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AndroidUserFavoriteRepository @Inject constructor(
    private val favoriteDao: FavoriteDao
) : UserFavoriteRepository {

    override fun isFavorite(item: ItemKey): Flow<Boolean> {
        return when (item) {
            is MovieKey.Hosted -> favoriteDao.isHostedFavorite(ItemType.MOVIE, item.streamingService, item.id)
            is TvShowKey.Hosted -> favoriteDao.isHostedFavorite(ItemType.TV_SHOW, item.streamingService, item.id)
            is MovieKey.Tmdb -> favoriteDao.isTmdbFavorite(ItemType.MOVIE, item.id)
            is TvShowKey.Tmdb -> favoriteDao.isTmdbFavorite(ItemType.TV_SHOW, item.id)
        }
    }

    override fun getUserFavorites(): Flow<Set<ItemKey>> {
        val tmdbItems = favoriteDao.getAllTmdbFavorites()
            .map { it.map { item -> item.fromDb() } }
        val hostedItems = favoriteDao.getAllHostedFavorites()
            .map { it.map { item -> item.fromDb() } }
        return combine(tmdbItems, hostedItems) { a, b -> (a + b).toSet() }
    }

    internal fun FavoriteTmdbItem.fromDb(): ItemKey {
        return when (type) {
            ItemType.TV_SHOW -> TvShowKey.Tmdb(tmdbItemId)
            ItemType.MOVIE -> MovieKey.Tmdb(tmdbItemId)
        }
    }

    internal fun FavoriteHostedItem.fromDb(): ItemKey {
        return when (type) {
            ItemType.TV_SHOW -> TvShowKey.Hosted(streamingService, key)
            ItemType.MOVIE -> MovieKey.Hosted(streamingService, key)
        }
    }

    internal fun ItemKey.Tmdb.toDb(): FavoriteTmdbItem {
        val type = when (this) {
            is TvShowKey.Tmdb -> ItemType.TV_SHOW
            is MovieKey.Tmdb -> ItemType.MOVIE
        }
        return FavoriteTmdbItem(type, id)
    }

    override suspend fun deleteUserFavorite(item: ItemKey) {
        when (item) {
            is ItemKey.Tmdb -> favoriteDao.deleteTmdbFavorite(item.toDb())
            is ItemKey.Hosted -> favoriteDao.deleteHostedFavorite(item.toDb())
        }
    }

    override suspend fun addUserFavorite(item: ItemKey) {
        when (item) {
            is ItemKey.Tmdb -> favoriteDao.insertTmdbFavorite(item.toDb())
            is ItemKey.Hosted -> favoriteDao.insertHostedFavorite(item.toDb())
        }
    }

    fun ItemKey.Hosted.toDb(): FavoriteHostedItem {
        val type = when (this) {
            is TvShowKey.Hosted -> ItemType.TV_SHOW
            is MovieKey.Hosted -> ItemType.MOVIE
        }
        return FavoriteHostedItem(type, streamingService, id)
    }
}