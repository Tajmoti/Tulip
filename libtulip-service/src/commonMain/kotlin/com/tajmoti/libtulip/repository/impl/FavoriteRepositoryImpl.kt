package com.tajmoti.libtulip.repository.impl

import com.tajmoti.libtulip.repository.UserFavoriteRepository
import com.tajmoti.libtulip.model.key.ItemKey
import com.tajmoti.libtulip.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow

class FavoriteRepositoryImpl(
    private val repo: UserFavoriteRepository
) : FavoritesRepository {

    override fun isFavorite(item: ItemKey): Flow<Boolean> {
        return repo.isFavorite(item)
    }

    override fun getUserFavorites(): Flow<Set<ItemKey>> {
        return repo.getUserFavorites()
    }

    override suspend fun deleteUserFavorite(item: ItemKey) {
        repo.deleteUserFavorite(item)
    }

    override suspend fun addUserFavorite(item: ItemKey) {
        repo.addUserFavorite(item)
    }
}