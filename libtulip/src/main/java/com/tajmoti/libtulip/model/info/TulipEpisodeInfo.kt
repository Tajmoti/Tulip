package com.tajmoti.libtulip.model.info

import com.tajmoti.libtulip.model.key.EpisodeKey

sealed interface TulipEpisodeInfo {
    val key: EpisodeKey
    val episodeNumber: Int
    val name: String?

    data class Hosted(
        override val key: EpisodeKey.Hosted,
        override val episodeNumber: Int,
        override val name: String?
    ) : TulipEpisodeInfo

    data class Tmdb(
        override val key: EpisodeKey.Tmdb,
        override val name: String,
        val overview: String?
    ) : TulipEpisodeInfo {
        override val episodeNumber = key.episodeNumber
    }
}