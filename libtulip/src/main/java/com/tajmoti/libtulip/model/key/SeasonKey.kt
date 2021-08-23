package com.tajmoti.libtulip.model.key

import java.io.Serializable

sealed class SeasonKey : Serializable {
    abstract val tvShowKey: TvShowKey

    /**
     * One-based season number or zero for "specials"
     */
    abstract val seasonNumber: Int

    data class Hosted(
        override val tvShowKey: TvShowKey.Hosted,
        override val seasonNumber: Int
    ) : SeasonKey() {
        val service = tvShowKey.service
        val tvShowId = tvShowKey.tvShowId
    }

    data class Tmdb(
        override val tvShowKey: TvShowKey.Tmdb,
        override val seasonNumber: Int
    ) : SeasonKey()
}