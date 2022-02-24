package com.tajmoti.libtulip.model.info

import com.tajmoti.libtulip.model.key.SeasonKey
import kotlinx.serialization.Serializable

sealed interface TulipSeasonInfo {
    val key: SeasonKey
    val episodes: List<TulipEpisodeInfo>

    @Serializable
    data class Hosted(
        override val key: SeasonKey.Hosted,
        override val episodes: List<TulipEpisodeInfo.Hosted>
    ) : TulipSeasonInfo

    @Serializable
    data class Tmdb(
        override val key: SeasonKey.Tmdb,
        val name: String,
        val overview: String?,
        override val episodes: List<TulipEpisodeInfo.Tmdb>
    ) : TulipSeasonInfo
}