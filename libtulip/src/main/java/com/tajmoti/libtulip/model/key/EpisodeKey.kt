package com.tajmoti.libtulip.model.key

sealed class EpisodeKey : StreamableKey {
    abstract val seasonKey: SeasonKey

    data class Hosted(
        override val seasonKey: SeasonKey.Hosted,
        val key: String
    ) : EpisodeKey(), StreamableKey.Hosted {
        val service = seasonKey.tvShowKey.streamingService
        val tvShowId = seasonKey.tvShowKey.tvShowId
        val episodeId = key
        override val streamingService = seasonKey.tvShowKey.streamingService
        override val streamableKey = key
    }

    data class Tmdb(
        override val seasonKey: SeasonKey.Tmdb,
        val episodeNumber: Int
    ) : EpisodeKey(), StreamableKey.Tmdb
}