package com.tajmoti.libtulip.service

import com.tajmoti.libtulip.model.LastPlayedPosition
import com.tajmoti.libtulip.model.key.ItemKey
import com.tajmoti.libtulip.model.key.StreamableKey
import kotlinx.coroutines.flow.Flow

interface PlayingHistoryRepository {

    fun getLastPlayedPosition(item: StreamableKey): Flow<LastPlayedPosition?>

    fun getLastPlayedPosition(item: ItemKey): Flow<LastPlayedPosition?>

    suspend fun setLastPlayedPosition(item: StreamableKey, progress: Float)

    suspend fun removeLastPlayedPosition(item: StreamableKey)
}