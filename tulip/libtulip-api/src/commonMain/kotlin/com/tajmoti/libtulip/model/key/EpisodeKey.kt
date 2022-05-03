package com.tajmoti.libtulip.model.key

import kotlinx.serialization.Serializable

@Serializable
sealed interface EpisodeKey : StreamableKey {
    val seasonKey: SeasonKey

    @Serializable
    data class Hosted(
        override val seasonKey: SeasonKey.Hosted,
        override val id: String
    ) : EpisodeKey, StreamableKey.Hosted {
        override val streamingService = seasonKey.tvShowKey.streamingService
        override val itemKey = seasonKey.tvShowKey
    }

    @Serializable
    data class Tmdb(
        override val seasonKey: SeasonKey.Tmdb,
        val episodeNumber: Int
    ) : EpisodeKey, StreamableKey.Tmdb {
        override val itemKey = seasonKey.tvShowKey
    }
}
