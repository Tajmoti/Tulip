package com.tajmoti.libtulip.service

import com.tajmoti.libtulip.model.info.StreamableInfo
import com.tajmoti.libtulip.model.key.StreamableKey
import com.tajmoti.libtulip.model.stream.StreamableInfoWithLangLinks

/**
 * Retrieves streams and maps them to their languages.
 */
interface LanguageMappingStreamService {

    /**
     * Retrieves the streams identified by [key] and maps language to each stream.
     * During loading, once the value of [StreamableInfo] is known, it is pushed to [infoConsumer].
     */
    suspend fun getStreamsWithLanguages(
        key: StreamableKey,
        infoConsumer: (StreamableInfo) -> Unit
    ): Result<StreamableInfoWithLangLinks> {
        return when (key) {
            is StreamableKey.Hosted -> getStreamsWithLanguagesByHostedKey(key, infoConsumer)
            is StreamableKey.Tmdb -> getStreamsByTmdbKey(key, infoConsumer)
        }
    }

    suspend fun getStreamsWithLanguagesByHostedKey(
        key: StreamableKey.Hosted,
        infoConsumer: (StreamableInfo) -> Unit
    ): Result<StreamableInfoWithLangLinks>

    suspend fun getStreamsByTmdbKey(
        key: StreamableKey.Tmdb,
        infoConsumer: (StreamableInfo) -> Unit
    ): Result<StreamableInfoWithLangLinks>
}