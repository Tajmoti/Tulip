package com.tajmoti.tulip.mapper.tmdb

import com.tajmoti.libtulip.model.Episode
import com.tajmoti.libtulip.model.key.*
import com.tajmoti.tulip.entity.tmdb.TmdbEpisode
import com.tajmoti.tulip.mapper.Mapper

class TmdbEpisodeMapper : Mapper<Episode.Tmdb, TmdbEpisode> {

    override fun fromDb(db: TmdbEpisode): Episode.Tmdb = with(db) {
        val tvShowKey = TvShowKey.Tmdb(db.tvId)
        val seasonKey = SeasonKey.Tmdb(tvShowKey, seasonNumber)
        val episodeKey = EpisodeKey.Tmdb(seasonKey, episodeNumber)
        return Episode.Tmdb(episodeKey, name, overview, stillPath, voteAverage)
    }

    override fun toDb(repo: Episode.Tmdb): TmdbEpisode = with(repo) {
        return TmdbEpisode(key.tvShowKey.id, key.seasonNumber, episodeNumber, name, overview, stillPath, voteAverage)
    }
}