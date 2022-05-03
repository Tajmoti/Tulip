package com.tajmoti.tulip.mapper

import com.tajmoti.libtulip.model.info.Episode
import com.tajmoti.libtulip.model.info.SeasonWithEpisodes
import com.tajmoti.tulip.db.entity.tmdb.DbTmdbSeason

class AndroidTmdbSeasonWithEpisodesMapper {
    private val mapper = AndroidTmdbSeasonMapper()

    fun fromDb(db: DbTmdbSeason, episodes: List<Episode.Tmdb>): SeasonWithEpisodes.Tmdb = with(db) {
        return SeasonWithEpisodes.Tmdb(mapper.fromDb(this), episodes)
    }

    fun toDb(repo: SeasonWithEpisodes.Tmdb): DbTmdbSeason = with(repo) {
        return DbTmdbSeason(season.key.tvShowKey.id, season.name, season.overview, season.seasonNumber)
    }
}