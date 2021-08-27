package com.tajmoti.libtulip.repository

import com.tajmoti.libtulip.model.hosted.HostedEpisode
import com.tajmoti.libtulip.model.hosted.HostedItem
import com.tajmoti.libtulip.model.hosted.HostedSeason
import com.tajmoti.libtulip.model.hosted.StreamingService
import com.tajmoti.libtulip.model.info.TmdbItemId
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.model.key.TvShowKey

interface HostedTvShowRepository {

    suspend fun getTvShowByKey(service: StreamingService, key: String): HostedItem.TvShow?

    suspend fun getTvShowByKey(key: TvShowKey.Hosted): HostedItem.TvShow? {
        return getTvShowByKey(key.service, key.tvShowId)
    }

    suspend fun getTvShowsByTmdbId(id: TmdbItemId.Tv): List<HostedItem.TvShow>

    suspend fun insertTvShow(show: HostedItem.TvShow)

    suspend fun getSeasonsByTvShow(service: StreamingService, tvShowKey: String): List<HostedSeason>

    suspend fun getSeasonsByTvShow(key: TvShowKey.Hosted): List<HostedSeason> {
        return getSeasonsByTvShow(key.service, key.tvShowId)
    }

    suspend fun getSeasonByKey(key: SeasonKey.Hosted): HostedSeason? {
        val tvShowKey = key.tvShowKey
        return getSeasonByNumber(tvShowKey.service, tvShowKey.tvShowId, key.seasonNumber)
    }

    suspend fun getSeasonByNumber(
        service: StreamingService,
        tvShowKey: String,
        seasonNumber: Int
    ): HostedSeason?

    suspend fun insertSeason(season: HostedSeason)

    suspend fun insertSeasons(seasons: List<HostedSeason>)


    suspend fun getEpisodesBySeason(
        service: StreamingService,
        tvShowKey: String,
        seasonNumber: Int
    ): List<HostedEpisode>

    suspend fun getEpisodesBySeason(key: SeasonKey.Hosted): List<HostedEpisode> {
        return getEpisodesBySeason(key.service, key.tvShowId, key.seasonNumber)
    }

    suspend fun getEpisodeByKey(key: EpisodeKey.Hosted): HostedEpisode? {
        return getEpisodeByKey(key.service, key.tvShowId, key.seasonKey.seasonNumber, key.episodeId)
    }

    suspend fun getEpisodeByKey(
        service: StreamingService,
        tvShowKey: String,
        seasonNumber: Int,
        key: String
    ): HostedEpisode?

    suspend fun getEpisodeByTmdbIdentifiers(
        tmdbItemId: TmdbItemId.Tv,
        seasonNumber: Int,
        episodeNumber: Int
    ): List<HostedEpisode>

    suspend fun insertEpisode(episode: HostedEpisode)

    suspend fun insertEpisodes(episodes: List<HostedEpisode>)
}
