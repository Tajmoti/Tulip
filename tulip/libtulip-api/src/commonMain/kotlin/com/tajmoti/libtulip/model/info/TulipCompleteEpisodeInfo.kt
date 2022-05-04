package com.tajmoti.libtulip.model.info

sealed interface TulipCompleteEpisodeInfo : StreamableInfo {
    val tvShow: TvShow
    val season: Season
    val episode: Episode

    data class Hosted(
        override val tvShow: TvShow.Hosted,
        override val season: Season.Hosted,
        override val episode: Episode.Hosted
    ) : TulipCompleteEpisodeInfo, StreamableInfo.Hosted {
        override val key = episode.key
        override val name = tvShow.name
        override val language = tvShow.language
    }

    data class Tmdb(
        override val tvShow: TvShow.Tmdb,
        override val season: Season.Tmdb,
        override val episode: Episode.Tmdb
    ) : TulipCompleteEpisodeInfo, StreamableInfo.Tmdb {
        override val key = episode.key
        override val name = episode.name
    }
}