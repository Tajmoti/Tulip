package com.tajmoti.tulip.mapper.tmdb

import com.tajmoti.libtulip.model.Episode
import com.tajmoti.libtulip.model.SeasonWithEpisodes
import com.tajmoti.tulip.entity.tmdb.TmdbSeason

class TmdbSeasonWithEpisodesMapper {
    private val mapper = TmdbSeasonMapper()

    fun fromDb(db: TmdbSeason, episodes: List<Episode.Tmdb>): SeasonWithEpisodes.Tmdb = with(db) {
        return SeasonWithEpisodes.Tmdb(mapper.fromDb(this), episodes)
    }

    fun toDb(repo: SeasonWithEpisodes.Tmdb): TmdbSeason = with(repo) {
        return TmdbSeason(season.key.tvShowKey.id, season.name, season.overview, season.seasonNumber)
    }
}