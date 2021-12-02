package com.tajmoti.libtulip.model.info

sealed interface TulipCompleteEpisodeInfo : StreamableInfo {
    val tvShow: TulipTvShowInfo
    val episodeInfo: TulipEpisodeInfo

    data class Hosted(
        override val tvShow: TulipTvShowInfo.Hosted,
        override val episodeInfo: TulipEpisodeInfo.Hosted
    ) : TulipCompleteEpisodeInfo, StreamableInfo.Hosted {
        override val key = episodeInfo.key
        override val name = tvShow.name
        override val language = tvShow.language
    }

    data class Tmdb(
        override val tvShow: TulipTvShowInfo.Tmdb,
        override val episodeInfo: TulipEpisodeInfo.Tmdb
    ) : TulipCompleteEpisodeInfo, StreamableInfo.Tmdb {
        override val key = episodeInfo.key
        override val name = tvShow.name
    }
}