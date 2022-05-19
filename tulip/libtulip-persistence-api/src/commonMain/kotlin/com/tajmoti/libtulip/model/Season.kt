package com.tajmoti.libtulip.model

import com.tajmoti.libtulip.model.key.SeasonKey
import kotlinx.serialization.Serializable

sealed interface Season {
    val key: SeasonKey
    val seasonNumber: Int

    @Serializable
    data class Hosted(
        override val key: SeasonKey.Hosted,
        override val seasonNumber: Int
    ) : Season

    @Serializable
    data class Tmdb(
        override val key: SeasonKey.Tmdb,
        val name: String,
        override val seasonNumber: Int,
        val overview: String?
    ) : Season
}