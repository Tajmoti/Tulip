package com.tajmoti.libtulip.facade

import com.tajmoti.libtulip.dto.LibraryItemDto
import com.tajmoti.libtulip.model.key.ItemKey
import kotlinx.coroutines.flow.Flow

interface UserFavoriteFacade {

    fun getUserFavorites(): Flow<List<LibraryItemDto>>

    suspend fun addItemToFavorites(key: ItemKey)

    suspend fun removeItemFromFavorites(key: ItemKey)
}