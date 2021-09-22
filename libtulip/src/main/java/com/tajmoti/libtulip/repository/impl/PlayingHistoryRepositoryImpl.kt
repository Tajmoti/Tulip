package com.tajmoti.libtulip.repository.impl

import com.tajmoti.libtulip.data.UserDataDataSource
import com.tajmoti.libtulip.model.history.LastPlayedPosition
import com.tajmoti.libtulip.model.key.ItemKey
import com.tajmoti.libtulip.model.key.StreamableKey
import com.tajmoti.libtulip.repository.PlayingHistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PlayingHistoryRepositoryImpl @Inject constructor(
    private val dataSource: UserDataDataSource
) : PlayingHistoryRepository {

    override fun getLastPlayedPosition(item: StreamableKey): Flow<LastPlayedPosition?> {
        return dataSource.getLastPlayedPosition(item)
    }

    override fun getLastPlayedPosition(item: ItemKey): Flow<LastPlayedPosition?> {
        return dataSource.getLastPlayedPosition(item)
    }

    override suspend fun setLastPlayedPosition(item: StreamableKey, position: Float?) {
        dataSource.setLastPlayedPosition(item, position)
    }
}