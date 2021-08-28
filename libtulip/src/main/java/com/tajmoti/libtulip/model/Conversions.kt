@file:Suppress("NOTHING_TO_INLINE")

package com.tajmoti.libtulip.model

import com.tajmoti.libtmdb.model.tv.Episode
import com.tajmoti.libtmdb.model.tv.Season
import com.tajmoti.libtulip.model.info.EpisodeInfoWithKey
import com.tajmoti.libtulip.model.info.TulipEpisodeInfo
import com.tajmoti.libtulip.model.info.TulipSeasonInfo
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtvprovider.EpisodeInfo

inline fun EpisodeInfo.pairEpInfoWithKey(hostedKey: TvShowKey.Hosted): EpisodeInfoWithKey {
    val key = EpisodeKey.Hosted(SeasonKey.Hosted(hostedKey, number), key)
    val info = TulipEpisodeInfo(number, name)
    return info to key
}

inline fun com.tajmoti.libtvprovider.Season.pairSeasonInfoWithKey(key: TvShowKey.Hosted): TulipSeasonInfo {
    val episodes = episodes.map { episode -> episode.pairEpInfoWithKey(key) }
    return TulipSeasonInfo(number, episodes)
}

inline fun Season.toTulipSeasonInfo(key: TvShowKey.Tmdb): TulipSeasonInfo {
    val seasonKey = SeasonKey.Tmdb(key, seasonNumber)
    val episodes = episodes.map { ep -> ep.toEpisodeInfoWithKey(seasonKey) }
    return TulipSeasonInfo(seasonNumber, episodes)
}

inline fun Episode.toEpisodeInfoWithKey(key: SeasonKey.Tmdb): EpisodeInfoWithKey {
    val info = TulipEpisodeInfo(episodeNumber, name)
    val epKey = EpisodeKey.Tmdb(key, episodeNumber)
    return EpisodeInfoWithKey(info, epKey)
}