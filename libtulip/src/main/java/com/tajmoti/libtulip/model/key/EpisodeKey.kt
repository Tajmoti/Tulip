package com.tajmoti.libtulip.model.key

sealed interface EpisodeKey : StreamableKey {
    val seasonKey: SeasonKey

    data class Hosted(
        override val seasonKey: SeasonKey.Hosted,
        override val id: String
    ) : EpisodeKey, StreamableKey.Hosted {
        override val streamingService = seasonKey.tvShowKey.streamingService
    }

    data class Tmdb(
        override val seasonKey: SeasonKey.Tmdb,
        val episodeNumber: Int
    ) : EpisodeKey, StreamableKey.Tmdb
}
