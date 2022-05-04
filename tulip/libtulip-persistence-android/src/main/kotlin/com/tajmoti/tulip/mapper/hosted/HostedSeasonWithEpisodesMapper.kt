package com.tajmoti.tulip.mapper.hosted

import com.tajmoti.libtulip.model.info.Episode
import com.tajmoti.libtulip.model.info.SeasonWithEpisodes
import com.tajmoti.tulip.entity.hosted.HostedSeason

class HostedSeasonWithEpisodesMapper {
    private val mapper = HostedSeasonMapper()

    fun fromDb(db: HostedSeason, episodes: List<Episode.Hosted>): SeasonWithEpisodes.Hosted = with(db) {
        return SeasonWithEpisodes.Hosted(mapper.fromDb(this), episodes)
    }

    fun toDb(repo: SeasonWithEpisodes.Hosted): HostedSeason = with(repo) {
        return HostedSeason(season.key.tvShowKey.streamingService, season.key.tvShowKey.id, season.seasonNumber)
    }
}