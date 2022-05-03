package com.tajmoti.libtulip.repository

import com.tajmoti.libtulip.model.key.ItemKey
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    /**
     * Checks whether the [item] was added to the user's favorites.
     */
    fun isFavorite(item: ItemKey): Flow<Boolean>

    /**
     * Returns all user's favorites.
     */
    fun getUserFavorites(): Flow<Set<ItemKey>>

    /**
     * Removes an [item] from the user's favorites.
     * Does nothing if the [item] isn't in their favorites.
     */
    suspend fun deleteUserFavorite(item: ItemKey)

    /**
     * Adds an [item] to the user's favorites.
     * Does nothing if the [item] is already in their favorites.
     */
    suspend fun addUserFavorite(item: ItemKey)
}