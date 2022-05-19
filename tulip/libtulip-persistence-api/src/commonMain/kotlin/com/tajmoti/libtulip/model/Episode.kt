package com.tajmoti.libtulip.model

import com.tajmoti.libtulip.model.key.EpisodeKey
import kotlinx.serialization.Serializable

sealed interface Episode : IdentityItem<EpisodeKey> {
    override val key: EpisodeKey
    val episodeNumber: Int
    val name: String?
    val overview: String?
    val stillPath: String?

    @Serializable
    data class Hosted(
        override val key: EpisodeKey.Hosted,
        override val episodeNumber: Int,
        override val name: String?,
        override val overview: String?,
        override val stillPath: String?,
    ) : Episode

    @Serializable
    data class Tmdb(
        override val key: EpisodeKey.Tmdb,
        override val name: String,
        override val overview: String?,
        override val stillPath: String?,
        val voteAverage: Float?
    ) : Episode {
        override val episodeNumber = key.episodeNumber
    }
}