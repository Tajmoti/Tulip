package com.tajmoti.libtulip.model.info

import com.tajmoti.libtulip.model.IdentityItem
import com.tajmoti.libtulip.model.key.EpisodeKey

sealed interface TulipEpisodeInfo : IdentityItem<EpisodeKey> {
    override val key: EpisodeKey
    val episodeNumber: Int
    val name: String?
    val overview: String?

    data class Hosted(
        override val key: EpisodeKey.Hosted,
        override val episodeNumber: Int,
        override val name: String?,
        override val overview: String?
    ) : TulipEpisodeInfo

    data class Tmdb(
        override val key: EpisodeKey.Tmdb,
        override val name: String,
        override val overview: String?,
        val stillPath: String?,
        val voteAverage: Float?
    ) : TulipEpisodeInfo {
        override val episodeNumber = key.episodeNumber
    }
}