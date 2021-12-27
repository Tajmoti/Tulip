@file:Suppress("NOTHING_TO_INLINE")

package com.tajmoti.libtulip.repository.impl

import com.tajmoti.libopensubtitles.model.search.SubtitleAttributes
import com.tajmoti.libopensubtitles.model.search.SubtitlesResponseData
import com.tajmoti.libtmdb.model.tv.Episode
import com.tajmoti.libtmdb.model.tv.Tv
import com.tajmoti.libtulip.model.info.TulipEpisodeInfo
import com.tajmoti.libtulip.model.info.TulipMovie
import com.tajmoti.libtulip.model.info.TulipSeasonInfo
import com.tajmoti.libtulip.model.info.TulipTvShowInfo
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.model.subtitle.SubtitleInfo
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
    tmdbId: TvShowKey.Tmdb?
): TulipTvShowInfo.Hosted {
    val seasons = seasons.map { it.fromNetwork(key) }
    return TulipTvShowInfo.Hosted(key, info, tmdbId, seasons)
}

inline fun MovieInfo.fromNetwork(
    key: MovieKey.Hosted,
    tmdbId: MovieKey.Tmdb?
): TulipMovie.Hosted {
    return TulipMovie.Hosted(key, info, tmdbId)
}

inline fun EpisodeInfo.fromNetwork(seasonKey: SeasonKey.Hosted): TulipEpisodeInfo.Hosted {
    val key = EpisodeKey.Hosted(seasonKey, key)
    return TulipEpisodeInfo.Hosted(key, number, name, overview)
}

inline fun Season.fromNetwork(tvShowKey: TvShowKey.Hosted): TulipSeasonInfo.Hosted {
    val key = SeasonKey.Hosted(tvShowKey, number)
    val episodes = episodes.map { it.fromNetwork(key) }
    return TulipSeasonInfo.Hosted(key, episodes)
}

fun Tv.fromNetwork(seasons: List<TulipSeasonInfo.Tmdb>): TulipTvShowInfo.Tmdb {
    val key = TvShowKey.Tmdb(id)
    val baseImageUrl = "https://image.tmdb.org/t/p/original"
    return TulipTvShowInfo.Tmdb(key, name, null, baseImageUrl + posterPath, baseImageUrl + backdropPath, seasons)
}

fun com.tajmoti.libtmdb.model.tv.Season.fromNetwork(tvShowKey: TvShowKey.Tmdb): TulipSeasonInfo.Tmdb {
    val key = SeasonKey.Tmdb(tvShowKey, seasonNumber)
    val episodes = episodes.map { it.fromNetwork(key) }
    return TulipSeasonInfo.Tmdb(key, name, overview, episodes)
}

fun Episode.fromNetwork(seasonKey: SeasonKey.Tmdb): TulipEpisodeInfo.Tmdb {
    val key = EpisodeKey.Tmdb(seasonKey, episodeNumber)
    return TulipEpisodeInfo.Tmdb(
        key,
        name,
        overview,
        stillPath,
        voteAverage.takeUnless { it == 0.0f })
}