package com.tajmoti.libtulip.model.info

import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.seasonNumber

sealed interface TulipCompleteEpisodeInfo : StreamableInfo {
    override val key: EpisodeKey
    val showName: String
    val seasonNumber: Int
    val episodeNumber: Int


    data class Hosted(
        override val key: EpisodeKey.Hosted,
        override val showName: String,
        val info: TulipEpisodeInfo.Hosted,
    ) : TulipCompleteEpisodeInfo, StreamableInfo.Hosted {
        override val name = info.name
        override val seasonNumber = info.key.seasonNumber
        override val episodeNumber = info.episodeNumber
    }

    data class Tmdb(
        override val key: EpisodeKey.Tmdb,
        override val showName: String,
        val info: TulipEpisodeInfo.Tmdb
    ) : TulipCompleteEpisodeInfo, StreamableInfo.Tmdb {
        override val episodeNumber = key.episodeNumber
        override val name = info.name
        override val seasonNumber = info.key.seasonNumber
    }
}