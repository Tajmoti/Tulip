package com.tajmoti.tulip.mapper

import com.tajmoti.libtulip.model.info.Episode
import com.tajmoti.libtulip.model.key.*
import com.tajmoti.tulip.db.entity.tmdb.DbTmdbEpisode

class AndroidTmdbEpisodeMapper : Mapper<Episode.Tmdb, DbTmdbEpisode> {

    override fun fromDb(db: DbTmdbEpisode): Episode.Tmdb = with(db) {
        val tvShowKey = TvShowKey.Tmdb(db.tvId)
        val seasonKey = SeasonKey.Tmdb(tvShowKey, seasonNumber)
        val episodeKey = EpisodeKey.Tmdb(seasonKey, episodeNumber)
        return Episode.Tmdb(episodeKey, name, overview, stillPath, voteAverage)
    }

    override fun toDb(repo: Episode.Tmdb): DbTmdbEpisode = with(repo) {
        return DbTmdbEpisode(key.tvShowKey.id, key.seasonNumber, episodeNumber, name, overview, stillPath, voteAverage)
    }
}