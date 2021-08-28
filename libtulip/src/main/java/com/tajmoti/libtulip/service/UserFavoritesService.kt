package com.tajmoti.libtulip.service

import com.tajmoti.libtulip.model.info.TulipItemInfo
import com.tajmoti.libtulip.model.key.ItemKey
import kotlinx.coroutines.flow.Flow

interface UserFavoritesService {

    suspend fun getUserFavorites(): List<TulipItemInfo>

    fun getUserFavoritesAsFlow(): Flow<List<TulipItemInfo>>

    suspend fun deleteUserFavorite(item: ItemKey)

    suspend fun addUserFavorite(item: ItemKey)
}