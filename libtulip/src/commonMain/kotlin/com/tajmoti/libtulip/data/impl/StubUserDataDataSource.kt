package com.tajmoti.libtulip.data.impl

import com.tajmoti.libtulip.data.UserDataDataSource
import com.tajmoti.libtulip.model.history.LastPlayedPosition
import com.tajmoti.libtulip.model.key.ItemKey
import com.tajmoti.libtulip.model.key.StreamableKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class StubUserDataDataSource : UserDataDataSource {
    override fun isFavorite(item: ItemKey): Flow<Boolean> {
        return flowOf(false)
    }

    override fun getUserFavorites(): Flow<List<ItemKey>> {
        return flowOf(emptyList())
    }

    override suspend fun deleteUserFavorite(item: ItemKey) {

    }

    override suspend fun addUserFavorite(item: ItemKey) {

    }

    override fun getLastPlayedPositionTmdb(key: ItemKey.Tmdb): Flow<LastPlayedPosition.Tmdb?> {
        return flowOf(null)
    }

    override fun getLastPlayedPositionTmdb(key: StreamableKey.Tmdb): Flow<LastPlayedPosition.Tmdb?> {
        return flowOf(null)
    }

    override fun getLastPlayedPositionHosted(key: ItemKey.Hosted): Flow<LastPlayedPosition.Hosted?> {
        return flowOf(null)
    }

    override fun getLastPlayedPositionHosted(key: StreamableKey.Hosted): Flow<LastPlayedPosition.Hosted?> {
        return flowOf(null)
    }

    override suspend fun setLastPlayedPosition(key: StreamableKey, progress: Float) {

    }

    override suspend fun removeLastPlayedPosition(key: StreamableKey) {

    }
}