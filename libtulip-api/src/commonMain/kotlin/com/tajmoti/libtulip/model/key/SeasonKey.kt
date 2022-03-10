package com.tajmoti.libtulip.model.key

import com.tajmoti.multiplatform.JvmSerializable
import kotlinx.serialization.Serializable

sealed interface SeasonKey : JvmSerializable {
    val tvShowKey: TvShowKey

    /**
     * One-based season number or zero for "specials"
     */
    val seasonNumber: Int

    @Serializable
    data class Hosted(
        override val tvShowKey: TvShowKey.Hosted,
        override val seasonNumber: Int
    ) : SeasonKey

    @Serializable
    data class Tmdb(
        override val tvShowKey: TvShowKey.Tmdb,
        override val seasonNumber: Int
    ) : SeasonKey
}