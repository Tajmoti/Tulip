package com.tajmoti.libtulip.model.info

import kotlinx.serialization.Serializable

sealed interface SeasonWithEpisodes {
    val season: Season
    val episodes: List<Episode>

    @Serializable
    data class Hosted(
        override val season: Season.Hosted,
        override val episodes: List<Episode.Hosted>
    ) : SeasonWithEpisodes

    @Serializable
    data class Tmdb(
        override val season: Season.Tmdb,
        override val episodes: List<Episode.Tmdb>
    ) : SeasonWithEpisodes
}