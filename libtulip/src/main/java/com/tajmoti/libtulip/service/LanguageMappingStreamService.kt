package com.tajmoti.libtulip.service

import arrow.core.Either
import com.tajmoti.libtulip.model.info.StreamableInfo
import com.tajmoti.libtulip.model.key.StreamableKey
import com.tajmoti.libtulip.model.stream.StreamableInfoWithLangLinks
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

typealias StreamsResult = Either<StreamableInfo?, StreamableInfoWithLangLinks>

/**
 * Retrieves streams and maps them to their languages.
 */
interface LanguageMappingStreamService {

    /**
     * Retrieves the streams identified by [key] and maps language to each stream.
     */
    suspend fun getStreamsWithLanguages(key: StreamableKey): Flow<StreamsResult> {
        return when (key) {
            is StreamableKey.Hosted -> flowOf(getStreamsWithLanguages(key))
            is StreamableKey.Tmdb -> getStreamsWithLanguages(key)
        }
    }

    suspend fun getStreamsWithLanguages(key: StreamableKey.Hosted): StreamsResult

    suspend fun getStreamsWithLanguages(key: StreamableKey.Tmdb): Flow<StreamsResult>
}