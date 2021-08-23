package com.tajmoti.libtulip.repository

import com.tajmoti.commonutils.awaitAll
import com.tajmoti.commonutils.mapToAsyncJobs
import com.tajmoti.libtmdb.model.movie.Movie
import com.tajmoti.libtmdb.model.tv.Episode
import com.tajmoti.libtmdb.model.tv.Season
import com.tajmoti.libtmdb.model.tv.Tv
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.service.takeIfNoneNull
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

interface ReadOnlyTvDataRepository {

    suspend fun getTv(tvId: Long): Tv?

    suspend fun getSeason(tvId: Long, seasonNumber: Int): Season?

    suspend fun getSeasons(tvId: Long): List<Season>

    suspend fun getEpisode(tvId: Long, seasonNumber: Int, episodeNumber: Int): Episode?

    suspend fun getEpisodes(tvId: Long, seasonNumber: Int): List<Episode>

    suspend fun getMovie(movieId: Long): Movie?

    suspend fun getFullEpisodeData(key: EpisodeKey.Tmdb): Triple<Tv, Season, Episode>? {
        val seasonKey = key.seasonKey
        val tvShowId = seasonKey.tvShowKey.id.id
        val coroutines = coroutineScope {
            val tv = async { getTv(tvShowId) }
            val season = async { getSeason(tvShowId, seasonKey.seasonNumber) }
            val episode = async { getEpisode(tvShowId, seasonKey.seasonNumber, key.episode) }
            Triple(tv, season, episode)
        }
        return coroutines.awaitAll().takeIfNoneNull()
    }

    suspend fun getFullTvShowData(key: TvShowKey.Tmdb): Pair<Tv, List<Season>>? {
        val tvShowId = key.id.id
        val tv = getTv(tvShowId)
            ?: return null
        val seasons = mapToAsyncJobs(tv.seasons) {
            getSeason(tvShowId, it.seasonNumber)
        }.takeIfNoneNull() ?: return null
        return tv to seasons
    }
}