package com.tajmoti.libtulip.repository.impl

import com.tajmoti.libtulip.data.UserDataDataSource
import com.tajmoti.libtulip.model.key.ItemKey
import com.tajmoti.libtulip.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FavoriteRepositoryImpl @Inject constructor(
    private val repo: UserDataDataSource
) : FavoritesRepository {

    override fun getUserFavoritesAsFlow(): Flow<List<ItemKey>> {
        return repo.getUserFavoritesAsFlow()
    }

    override suspend fun deleteUserFavorite(item: ItemKey) {
        repo.deleteUserFavorite(item)
    }

    override suspend fun addUserFavorite(item: ItemKey) {
        repo.addUserFavorite(item)
    }
}