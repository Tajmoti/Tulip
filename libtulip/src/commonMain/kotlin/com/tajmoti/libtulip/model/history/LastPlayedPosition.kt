package com.tajmoti.libtulip.model.history

import com.tajmoti.libtulip.model.key.StreamableKey

sealed interface LastPlayedPosition {
    val key: StreamableKey

    /**
     * Item playing progress in percent (represented as a float from 0.0 to 1.0)
     */
    val progress: Float

    data class Tmdb(
        override val key: StreamableKey.Tmdb,
        override val progress: Float
    ) : LastPlayedPosition

    data class Hosted(
        override val key: StreamableKey.Hosted,
        override val progress: Float
    ) : LastPlayedPosition
}
