@file:OptIn(ExperimentalCoroutinesApi::class)

package com.tajmoti.libtulip.repository

import com.tajmoti.commonutils.combineNonEmpty
import com.tajmoti.commonutils.flatMap
import com.tajmoti.libtulip.misc.job.NetworkResult
import com.tajmoti.libtulip.model.MissingEntityException
import com.tajmoti.libtulip.model.info.*
import com.tajmoti.libtulip.model.key.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

/**
 * Retrieves information about either a movie, or a TV show on a specific streaming site by its [key].
 * The returned flow may never complete, and it may emit an updated value at any time!
 */
fun HostedTvDataRepository.getItemByKey(key: ItemKey.Hosted) = when (key) {
    is TvShowKey.Hosted -> getTvShow(key)
    is MovieKey.Hosted -> getMovie(key)
}

/**
 * Retrieves information about a streamable (a TV show episode or a movie) on a specific streaming site by its [key].
 * The returned flow may never complete, and it may emit an updated value at any time!
 */
fun HostedTvDataRepository.getStreamableInfo(key: StreamableKey.Hosted) = when (key) {
    is EpisodeKey.Hosted -> getEpisodeInfo(key)
    is MovieKey.Hosted -> getMovie(key).map { it.toResult() }
}

/**
 * Retrieves a season by its [key].
 * The returned flow may never complete, and it may emit an updated value at any time!
 */
fun HostedTvDataRepository.getSeason(key: SeasonKey.Hosted) = getTvShow(key.tvShowKey)
    .map { it.convert { showInfo -> showInfo.findSeasonOrNull(key) } }

/**
 * Retrieves all seasons of a TV show by the TV show [key].
 * The returned flow may never complete, and it may emit an updated value at any time!
 */
fun HostedTvDataRepository.getSeasons(key: TvShowKey.Hosted) = getTvShow(key)
    .map { it.map { tvShow -> tvShow.seasons } }

/**
 * Retrieves an episode by its [key].
 * The returned flow may never complete, and it may emit an updated value at any time!
 */
fun HostedTvDataRepository.getEpisodeInfo(key: EpisodeKey.Hosted) = getTvShow(key.tvShowKey)
    .map { netResult -> netResult.toResult().flatMap { tvShow -> tvShow.findCompleteEpisodeInfoAsResult(key) } }


/**
 * Retrieves information about a streamable (a TV show episode or a movie)
 * on a specific streaming site by a TMDB key that was assigned to it during search.
 * The returned flow may never complete, and it may emit an updated value at any time!
 */
fun HostedTvDataRepository.getStreamableInfoByTmdbKey(
    mappingRepository: ItemMappingRepository,
    key: StreamableKey.Tmdb
) = when (key) {
    is MovieKey.Tmdb -> getMoviesByTmdbKey(mappingRepository, key)
    is EpisodeKey.Tmdb -> getEpisodesByTmdbKey(mappingRepository, key)
}

/**
 * Retrieves information about a TV show on a specific streaming site
 * by a TMDB key that was assigned to it during search.
 * The returned flow may never complete, and it may emit an updated value at any time!
 */
fun HostedTvDataRepository.getTvShowsByTmdbKey(mappingRepository: ItemMappingRepository, key: TvShowKey.Tmdb) =
    mappingRepository.getHostedTvShowKeysByTmdbKey(key)
        .flatMapLatest { tvShowKeys ->
            tvShowKeys.map { getTvShow(it) }
                .combineNonEmpty()
                .mapNetworkResultToResultInListFlow()
        }

/**
 * Retrieves information about a TV show episode on a specific streaming site
 * by a TMDB key that was assigned to it during search.
 * The returned flow may never complete, and it may emit an updated value at any time!
 */
fun HostedTvDataRepository.getEpisodesByTmdbKey(
    mappingRepository: ItemMappingRepository,
    key: EpisodeKey.Tmdb
) = getTvShowsByTmdbKey(mappingRepository, key.tvShowKey)
    .map { tvListResult ->
        tvListResult.map { tvList ->
            tvList.flatMap { tv -> tv.findCompleteEpisodeFromTvAsResult(key) }
        }
    }

/**
 * Retrieves information about a movie on a specific streaming site
 * by a TMDB key that was assigned to it during search.
 * The returned flow may never complete, and it may emit an updated value at any time!
 */
fun HostedTvDataRepository.getMoviesByTmdbKey(mappingRepository: ItemMappingRepository, key: MovieKey.Tmdb) =
    mappingRepository.getHostedMovieKeysByTmdbKey(key)
        .flatMapLatest { movieKeyList ->
            movieKeyList.map { getMovie(it) }
                .combineNonEmpty()
                .mapNetworkResultToResultInListFlow()
        }

private fun <T> T?.toResultIfMissing(): Result<T> {
    return this?.let { notNull -> Result.success(notNull) } ?: Result.failure(MissingEntityException)
}

private fun TulipTvShowInfo.Hosted.findEpisodeAsResult(
    key: EpisodeKey.Tmdb
) = findEpisodeOrNull(key).toResultIfMissing()

private fun TulipTvShowInfo.Hosted.findCompleteEpisodeInfoAsResult(
    key: EpisodeKey.Hosted
) = findCompleteEpisodeInfo(key).toResultIfMissing()

private fun TulipTvShowInfo.Hosted.findCompleteEpisodeFromTvAsResult(
    key: EpisodeKey.Tmdb
) = findEpisodeAsResult(key).map { episode -> TulipCompleteEpisodeInfo.Hosted(this, episode) }

private fun <T> Flow<List<NetworkResult<T>>>.mapNetworkResultToResultInListFlow(): Flow<List<Result<T>>> {
    return map { it.map { networkResult -> networkResult.toResult() } }
}