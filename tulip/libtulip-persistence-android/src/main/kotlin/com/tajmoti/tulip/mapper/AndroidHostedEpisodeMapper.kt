package com.tajmoti.tulip.mapper

import com.tajmoti.libtulip.model.info.Episode
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.tulip.db.entity.hosted.DbEpisode

class AndroidHostedEpisodeMapper : Mapper<Episode.Hosted, DbEpisode> {

    override fun fromDb(db: DbEpisode): Episode.Hosted = with(db) {
        val tvShowKey = TvShowKey.Hosted(db.service, db.tvShowKey)
        val seasonKey = SeasonKey.Hosted(tvShowKey, db.seasonNumber)
        val episodeKey = EpisodeKey.Hosted(seasonKey, db.key)
        return Episode.Hosted(episodeKey, number, name, overview, stillPath)
    }

    override fun toDb(repo: Episode.Hosted): DbEpisode = with(repo) {
        return DbEpisode(
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