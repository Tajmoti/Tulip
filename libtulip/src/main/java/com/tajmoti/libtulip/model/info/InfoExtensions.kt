package com.tajmoti.libtulip.model.info

import com.tajmoti.libtulip.model.hosted.StreamingService
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.model.key.seasonNumber

val TulipSeasonInfo.seasonNumber: Int
    get() = key.seasonNumber

val TulipItem.Hosted.streamingService: StreamingService
    get() = key.streamingService

fun TulipTvShowInfo.Hosted.findSeasonOrNull(key: EpisodeKey): TulipSeasonInfo.Hosted? {
    return seasons.firstOrNull { it.seasonNumber == key.seasonKey.seasonNumber }
}

fun TulipTvShowInfo.Hosted.findSeasonOrNull(key: SeasonKey.Hosted): TulipSeasonInfo.Hosted? {
    return seasons.firstOrNull { season -> season.key == key }
}

fun TulipTvShowInfo.Hosted.findEpisodeOrNull(key: EpisodeKey.Tmdb): TulipEpisodeInfo.Hosted? {
    return findSeasonOrNull(key)?.findEpisodeOrNull(key)
}

fun TulipTvShowInfo.Hosted.findEpisodeOrNull(key: EpisodeKey.Hosted): TulipEpisodeInfo.Hosted? {
    return findSeasonOrNull(key)?.findEpisodeOrNull(key)
}

fun TulipSeasonInfo.Hosted.findEpisodeOrNull(key: EpisodeKey.Tmdb): TulipEpisodeInfo.Hosted? {
    return episodes.firstOrNull { it.episodeNumber == key.episodeNumber }
}

fun TulipSeasonInfo.Hosted.findEpisodeOrNull(key: EpisodeKey.Hosted): TulipEpisodeInfo.Hosted? {
    return episodes.firstOrNull { it.key.id == key.id }
}

fun TulipTvShowInfo.Hosted.findCompleteEpisodeInfo(key: EpisodeKey.Hosted): TulipCompleteEpisodeInfo.Hosted? {
    return findEpisodeOrNull(key)?.let { episode -> TulipCompleteEpisodeInfo.Hosted(this, episode) }
}

val TulipCompleteEpisodeInfo.showName: String
    get() = tvShow.name

val TulipCompleteEpisodeInfo.seasonNumber: Int
    get() = episodeInfo.key.seasonNumber

val TulipCompleteEpisodeInfo.episodeNumber: Int
    get() = episodeInfo.episodeNumber