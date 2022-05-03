package com.tajmoti.libtulip.repository.impl

import com.tajmoti.libtulip.repository.UserLastPlayedPositionRepository
import com.tajmoti.libtulip.model.history.LastPlayedPosition
import com.tajmoti.libtulip.model.key.ItemKey
import com.tajmoti.libtulip.model.key.StreamableKey
import com.tajmoti.libtulip.repository.PlayingHistoryRepository
import kotlinx.coroutines.flow.Flow

class PlayingHistoryRepositoryImpl(
    private val dataSource: UserLastPlayedPositionRepository
) : PlayingHistoryRepository {

    override fun getLastPlayedPosition(item: StreamableKey): Flow<LastPlayedPosition?> {
        return dataSource.getLastPlayedPosition(item)
    }

    override fun getLastPlayedPosition(item: ItemKey): Flow<LastPlayedPosition?> {
        return dataSource.getLastPlayedPosition(item)
    }

    override suspend fun setLastPlayedPosition(item: StreamableKey, progress: Float) {
        dataSource.setLastPlayedPosition(item, progress)
    }

    override suspend fun removeLastPlayedPosition(item: StreamableKey) {
        dataSource.removeLastPlayedPosition(item)
    }
}