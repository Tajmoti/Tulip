package com.tajmoti.libtulip.repository

import com.tajmoti.libtulip.model.StreamingService
import com.tajmoti.libtulip.model.TulipEpisode
import com.tajmoti.libtulip.model.TulipSeason
import com.tajmoti.libtulip.model.TulipTvShow
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.model.key.TvShowKey

interface TvShowRepository {

    suspend fun getTvShowByKey(service: StreamingService, key: String): TulipTvShow?

    suspend fun getTvShowByKey(key: TvShowKey): TulipTvShow? {
        return getTvShowByKey(key.service, key.tvShowId)
    }

    suspend fun insertTvShow(show: TulipTvShow)

    suspend fun getSeasonByKey(
        service: StreamingService,
        tvShowKey: String,
        key: String
    ): TulipSeason?

    suspend fun getSeasonByKey(key: SeasonKey): TulipSeason? {
        return getSeasonByKey(key.service, key.tvShowId, key.seasonId)
    }

    suspend fun insertSeason(season: TulipSeason)


    suspend fun getEpisodesBySeason(
        service: StreamingService,
        tvShowKey: String,
        seasonKey: String
    ): List<TulipEpisode>

    suspend fun getEpisodesBySeason(seasonKey: SeasonKey): List<TulipEpisode> {
        return getEpisodesBySeason(seasonKey.service, seasonKey.tvShowId, seasonKey.seasonId)
    }

    suspend fun getEpisodesByKey(
        service: StreamingService,
        tvShowKey: String,
        seasonKey: String,
        key: String
    ): TulipEpisode?

    suspend fun insertEpisode(episode: TulipEpisode)
}
