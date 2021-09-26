@file:Suppress("NOTHING_TO_INLINE")

package com.tajmoti.libtulip.repository.impl

import com.tajmoti.libopensubtitles.model.search.SubtitleAttributes
import com.tajmoti.libopensubtitles.model.search.SubtitlesResponseData
import com.tajmoti.libtulip.model.info.TulipEpisodeInfo
import com.tajmoti.libtulip.model.info.TulipMovie
import com.tajmoti.libtulip.model.info.TulipSeasonInfo
import com.tajmoti.libtulip.model.info.TulipTvShowInfo
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.model.subtitle.SubtitleInfo
import com.tajmoti.libtulip.model.tmdb.TmdbItemId
import com.tajmoti.libtvprovider.EpisodeInfo
import com.tajmoti.libtvprovider.MovieInfo
import com.tajmoti.libtvprovider.Season
import com.tajmoti.libtvprovider.TvShowInfo

inline fun SubtitlesResponseData.fromApi(): SubtitleInfo? {
    return attributes.fromApi()
}

inline fun SubtitleAttributes.fromApi(): SubtitleInfo? {
    val file = data.firstOrNull()?.fileId ?: return null
    return SubtitleInfo(release, language, subtitleId, legacySubtitleId, file)
}

inline fun TvShowInfo.fromNetwork(
    key: TvShowKey.Hosted,
    tmdbId: TmdbItemId.Tv?
): TulipTvShowInfo.Hosted {
    val seasons = seasons.map { it.fromNetwork(key) }
    return TulipTvShowInfo.Hosted(key, info, tmdbId, seasons)
}

inline fun MovieInfo.fromNetwork(
    key: MovieKey.Hosted,
    tmdbId: TmdbItemId.Movie?
): TulipMovie.Hosted {
    return TulipMovie.Hosted(key, info, tmdbId)
}

inline fun EpisodeInfo.fromNetwork(seasonKey: SeasonKey.Hosted): TulipEpisodeInfo.Hosted {
    val key = EpisodeKey.Hosted(seasonKey, key)
    return TulipEpisodeInfo.Hosted(key, number, name)
}

inline fun Season.fromNetwork(tvShowKey: TvShowKey.Hosted): TulipSeasonInfo.Hosted {
    val key = SeasonKey.Hosted(tvShowKey, number)
    val episodes = episodes.map { it.fromNetwork(key) }
    return TulipSeasonInfo.Hosted(key, episodes)
}