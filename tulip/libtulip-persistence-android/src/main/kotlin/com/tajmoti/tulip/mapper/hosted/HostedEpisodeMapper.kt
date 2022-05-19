package com.tajmoti.tulip.mapper.hosted

import com.tajmoti.libtulip.model.Episode
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.tulip.entity.hosted.HostedEpisode
import com.tajmoti.tulip.mapper.Mapper

class HostedEpisodeMapper : Mapper<Episode.Hosted, HostedEpisode> {

    override fun fromDb(db: HostedEpisode): Episode.Hosted = with(db) {
        val tvShowKey = TvShowKey.Hosted(db.service, db.tvShowKey)
        val seasonKey = SeasonKey.Hosted(tvShowKey, db.seasonNumber)
        val episodeKey = EpisodeKey.Hosted(seasonKey, db.key)
        return Episode.Hosted(episodeKey, number, name, overview, stillPath)
    }

    override fun toDb(repo: Episode.Hosted): HostedEpisode = with(repo) {
        return HostedEpisode(
            key.seasonKey.tvShowKey.streamingService,
            key.seasonKey.tvShowKey.id,
            key.seasonKey.seasonNumber,
            key.id,
            episodeNumber,
            name,
            overview,
            stillPath
        )
    }
}