package com.tajmoti.libtulip.model.info

import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.model.tmdb.TmdbItemId
import com.tajmoti.libtvprovider.TvItemInfo

interface TulipTvShowInfo {
    val seasons: List<TulipSeasonInfo>

    class Tmdb(
        override val key: TvShowKey.Tmdb,
        override val name: String,
        override val overview: String?,
        override val posterPath: String?,
        override val backdropPath: String?,
        override val seasons: List<TulipSeasonInfo.Tmdb>
    ) : TulipTvShowInfo, TulipItem.Tmdb

    class Hosted(
        override val key: TvShowKey.Hosted,
        val info: TvItemInfo,
        override val tmdbId: TmdbItemId.Tv?,
        override val seasons: List<TulipSeasonInfo.Hosted>
    ) : TulipTvShowInfo, TulipItem.Hosted {
        override val name = info.name
        override val language = LanguageCode(info.language)
    }
}