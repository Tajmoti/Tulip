package com.tajmoti.libtulip.repository

import com.tajmoti.libtulip.model.hosted.*
import com.tajmoti.libtulip.model.key.*
import com.tajmoti.libtulip.model.tmdb.TmdbItemId
import kotlinx.coroutines.flow.Flow

interface HostedTvDataRepository {
    suspend fun getItemByKey(key: ItemKey.Hosted): HostedItem? {
        return when (key) {
            is TvShowKey.Hosted -> getTvShowByKey(key)
            is MovieKey.Hosted -> getMovieByKey(key)
        }
    }

    fun getTvShowByKeyAsFlow(service: StreamingService, key: String): Flow<HostedItem.TvShow?>

    fun getTvShowByKeyFlow(key: TvShowKey.Hosted): Flow<HostedItem.TvShow?> {
        return getTvShowByKeyAsFlow(key.streamingService, key.tvShowId)
    }

    suspend fun getTvShowByKey(service: StreamingService, key: String): HostedItem.TvShow?

    suspend fun getTvShowByKey(key: TvShowKey.Hosted): HostedItem.TvShow? {
        return getTvShowByKey(key.streamingService, key.tvShowId)
    }

    suspend fun getTvShowsByTmdbId(id: TmdbItemId.Tv): List<HostedItem.TvShow>

    suspend fun insertTvShow(show: HostedItem.TvShow)

    suspend fun getSeasonsByTvShow(service: StreamingService, tvShowKey: String): List<HostedSeason>

    suspend fun getSeasonsByTvShow(key: TvShowKey.Hosted): List<HostedSeason> {
        return getSeasonsByTvShow(key.streamingService, key.tvShowId)
    }

    suspend fun getSeasonByKey(key: SeasonKey.Hosted): HostedSeason? {
        val tvShowKey = key.tvShowKey
        return getSeasonByNumber(tvShowKey.streamingService, tvShowKey.tvShowId, key.seasonNumber)
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

    suspend fun getMovieByKey(service: StreamingService, key: String): HostedItem.Movie?

    suspend fun getMovieByKey(key: MovieKey.Hosted): HostedItem.Movie? {
        return getMovieByKey(key.service, key.movieId)
    }

    suspend fun getMovieByTmdbIdentifiers(tmdbItemId: TmdbItemId.Movie): List<HostedMovie>

    suspend fun insertMovie(movie: HostedItem.Movie)
}
