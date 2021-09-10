package com.tajmoti.libtulip.data

import com.tajmoti.libtulip.model.key.ItemKey
import kotlinx.coroutines.flow.Flow

interface UserDataDataSource {

    suspend fun getUserFavorites(): List<ItemKey>

    fun getUserFavoritesAsFlow(): Flow<List<ItemKey>>

    suspend fun deleteUserFavorite(item: ItemKey)

    suspend fun addUserFavorite(item: ItemKey)
}