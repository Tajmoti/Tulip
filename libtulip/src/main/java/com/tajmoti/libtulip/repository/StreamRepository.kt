package com.tajmoti.libtulip.repository

import com.tajmoti.libtulip.model.key.StreamableKey
import kotlinx.coroutines.flow.Flow

interface StreamRepository {
    fun getStreamsByKey(key: StreamableKey): Flow<StreamsResult> {
        return when (key) {
            is StreamableKey.Hosted -> getStreamsByKey(key)
            is StreamableKey.Tmdb -> getStreamsByKey(key)
        }
    }

    fun getStreamsByKey(key: StreamableKey.Hosted): Flow<StreamsResult>

    fun getStreamsByKey(key: StreamableKey.Tmdb): Flow<StreamsResult>
}