package com.tajmoti.tulip.mapper

import com.tajmoti.libtulip.model.info.Episode
import com.tajmoti.libtulip.model.info.SeasonWithEpisodes
import com.tajmoti.tulip.db.entity.hosted.DbSeason

class AndroidHostedSeasonWithEpisodesMapper {
    private val mapper = AndroidHostedSeasonMapper()

    fun fromDb(db: DbSeason, episodes: List<Episode.Hosted>): SeasonWithEpisodes.Hosted = with(db) {
        return SeasonWithEpisodes.Hosted(mapper.fromDb(this), episodes)
    }

    fun toDb(repo: SeasonWithEpisodes.Hosted): DbSeason = with(repo) {
        return DbSeason(season.key.tvShowKey.streamingService, season.key.tvShowKey.id, season.seasonNumber)
    }
}