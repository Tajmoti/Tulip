package com.tajmoti.libtulip.data

import com.tajmoti.libtulip.model.history.LastPlayedPosition
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.ItemKey
import com.tajmoti.libtulip.model.key.StreamableKey
import kotlinx.coroutines.flow.Flow

interface UserDataDataSource {
    fun isFavorite(item: ItemKey): Flow<Boolean>

    suspend fun getUserFavorites(): List<ItemKey>

    fun getUserFavoritesAsFlow(): Flow<List<ItemKey>>

    suspend fun deleteUserFavorite(item: ItemKey)

    suspend fun addUserFavorite(item: ItemKey)


    fun getLastPlayedPosition(key: ItemKey): Flow<LastPlayedPosition?>

    fun getLastPlayedPositionTmdb(key: ItemKey.Tmdb): Flow<LastPlayedPosition.Tmdb?>

    fun getLastPlayedPositionHosted(key: ItemKey.Hosted): Flow<LastPlayedPosition.Hosted?>


    fun getLastPlayedPosition(key: StreamableKey): Flow<LastPlayedPosition?>

    fun getLastPlayedPositionTmdb(key: StreamableKey.Tmdb): Flow<LastPlayedPosition.Tmdb?>

    fun getLastPlayedPositionHosted(key: StreamableKey.Hosted): Flow<LastPlayedPosition.Hosted?>


    suspend fun setLastPlayedPosition(key: StreamableKey, progress: Float?)
}