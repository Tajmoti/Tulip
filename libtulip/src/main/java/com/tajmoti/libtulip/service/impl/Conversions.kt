package com.tajmoti.libtulip.service.impl

import com.tajmoti.libtulip.model.hosted.HostedEpisode
import com.tajmoti.libtulip.model.hosted.HostedItem
import com.tajmoti.libtulip.model.hosted.HostedSeason
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtvprovider.EpisodeInfo
import com.tajmoti.libtvprovider.Season
import com.tajmoti.libtvprovider.TvItemInfo


internal fun HostedItem.TvShow.toInfo(key: TvShowKey.Hosted): TvItemInfo {
    return TvItemInfo(key.tvShowId, name, language, firstAirDateYear)
}

internal fun HostedSeason.toInfo(episodes: List<EpisodeInfo>): Season {
    return Season(tvShowKey, number, episodes)
}

internal fun HostedEpisode.toInfo(): EpisodeInfo {
    return EpisodeInfo(key, number, name)
}