package com.tajmoti.libtulip.service

import com.tajmoti.libtulip.model.key.StreamableKey
import com.tajmoti.libtulip.model.stream.UnloadedVideoStreamRef
import kotlinx.coroutines.flow.Flow

/**
 * Retrieves streams for a [StreamableKey].
 */
interface StreamService {
    /**
     * Retrieves streams for [key].
     * The flow will emit multiple values as more search results are loaded in.
     */
    fun getStreamsByKey(key: StreamableKey.Hosted): Flow<Result<List<UnloadedVideoStreamRef>>>

    /**
     * Retrieves streams for [key].
     * The flow will emit multiple values as more search results are loaded in.
     */
    fun getStreamsByKey(key: StreamableKey.Tmdb): Flow<Result<List<UnloadedVideoStreamRef>>>

    /**
     * Retrieves streams for [key].
     * The flow will emit multiple values as more search results are loaded in.
     */
    fun getStreamsByKey(key: StreamableKey): Flow<Result<List<UnloadedVideoStreamRef>>> {
        return when (key) {
            is StreamableKey.Hosted -> getStreamsByKey(key)
            is StreamableKey.Tmdb -> getStreamsByKey(key)
        }
    }
}