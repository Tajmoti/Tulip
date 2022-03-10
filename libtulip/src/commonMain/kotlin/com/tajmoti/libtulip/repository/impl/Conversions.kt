@file:Suppress("NOTHING_TO_INLINE")

package com.tajmoti.libtulip.repository.impl

import com.tajmoti.libopensubtitles.model.search.SubtitleAttributes
import com.tajmoti.libopensubtitles.model.search.SubtitlesResponseData
import com.tajmoti.libtulip.model.hosted.TvItemInfo
import com.tajmoti.libtulip.model.info.*
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.model.subtitle.SubtitleInfo
import com.tajmoti.libtvprovider.model.EpisodeInfo
import com.tajmoti.libtvprovider.model.Season
import com.tajmoti.libtvprovider.model.TvItem

inline fun SubtitlesResponseData.fromApi(): SubtitleInfo? {
    return attributes.fromApi()
}

inline fun SubtitleAttributes.fromApi(): SubtitleInfo? {
    val file = data.firstOrNull()?.fileId ?: return null
    return SubtitleInfo(release, language, subtitleId, legacySubtitleId, file)
}

inline fun TvItem.TvShow.fromNetwork(
    key: TvShowKey.Hosted,
    tmdbId: TvShowKey.Tmdb?
): TulipTvShowInfo.Hosted {
    val seasons = seasons.map { it.fromNetwork(key) }
    return TulipTvShowInfo.Hosted(key, info.name, LanguageCode(info.language), info.firstAirDateYear, tmdbId, seasons)
}

inline fun TvItem.Movie.fromNetwork(
    key: MovieKey.Hosted,
    tmdbId: MovieKey.Tmdb?
): TulipMovie.Hosted {
    return TulipMovie.Hosted(key, TvItemInfo(info.name, info.language, info.firstAirDateYear), tmdbId)
}

inline fun EpisodeInfo.fromNetwork(seasonKey: SeasonKey.Hosted): TulipEpisodeInfo.Hosted {
    val key = EpisodeKey.Hosted(seasonKey, key)
    return TulipEpisodeInfo.Hosted(key, number, name, overview, stillPath)
}

inline fun Season.fromNetwork(tvShowKey: TvShowKey.Hosted): TulipSeasonInfo.Hosted {
    val key = SeasonKey.Hosted(tvShowKey, number)
    val episodes = episodes.map { it.fromNetwork(key) }
    return TulipSeasonInfo.Hosted(key, episodes)
}
