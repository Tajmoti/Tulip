package com.tajmoti.libtulip.repository

import com.tajmoti.libtulip.model.key.ItemKey
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {

    fun isFavorite(item: ItemKey): Flow<Boolean>

    fun getUserFavorites(): Flow<List<ItemKey>>

    suspend fun deleteUserFavorite(item: ItemKey)

    suspend fun addUserFavorite(item: ItemKey)
}