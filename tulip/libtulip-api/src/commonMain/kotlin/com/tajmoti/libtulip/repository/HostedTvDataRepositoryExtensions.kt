@file:OptIn(ExperimentalCoroutinesApi::class)

package com.tajmoti.libtulip.repository

import com.tajmoti.commonutils.combine
import com.tajmoti.commonutils.combineNonEmpty
import com.tajmoti.libtulip.misc.job.NetworkResult
import com.tajmoti.libtulip.model.MissingEntityException
import com.tajmoti.libtulip.model.info.*
import com.tajmoti.libtulip.model.key.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
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
 * Retrieves all seasons of a TV show by the TV show [key].
 * The returned flow may never complete, and it may emit an updated value at any time!
 */
fun HostedTvDataRepository.getSeasons(key: TvShowKey.Hosted) = getTvShow(key)
    .map { it.map { tvShow -> tvShow.seasons } }


/**
 * Retrieves information about a streamable (a TV show episode or a movie)
 * on a specific streaming site by a TMDB key that was assigned to it during search.
 * The returned flow may never complete, and it may emit an updated value at any time!
 */
fun HostedTvDataRepository.getStreamableInfoByTmdbKey(
    mappingRepository: ItemMappingRepository,
    key: StreamableKey.Tmdb
): Flow<List<Result<StreamableInfo.Hosted>>> = when (key) {
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
): Flow<List<Result<TulipCompleteEpisodeInfo.Hosted>>> {
    return getTvShowsByTmdbKey(mappingRepository, key.tvShowKey)
        .flatMapLatest { tvListResult ->
            tvListResult.map { tvResult ->
                tvResult.map { selectEpisodeForTv(it, key) }.getOrNull()
                    ?: flowOf(null)
            }.combine()
        }
        .map { episodes ->
            episodes.map { episode ->
                episode?.let { Result.success(it) } ?: Result.failure(
                    MissingEntityException
                )
            }
        }
}

private fun HostedTvDataRepository.selectEpisodeForTv(
    tv: TvShow.Hosted,
    key: EpisodeKey.Tmdb
): Flow<TulipCompleteEpisodeInfo.Hosted?> {
    return tv.seasons.firstOrNull { it.seasonNumber == key.seasonNumber }
        ?.let { selectEpisodeForSeason(key, it, tv) } ?: flowOf(null)
}

private fun HostedTvDataRepository.selectEpisodeForSeason(
    query: EpisodeKey.Tmdb,
    season: Season.Hosted,
    tvShow: TvShow.Hosted
): Flow<TulipCompleteEpisodeInfo.Hosted?> {
    return getSeasonWithEpisodes(season.key)
        .map { seasonResult -> seasonResult.map { season -> selectEpisodeForSeason(season, tvShow, query) }.data }
}

private fun selectEpisodeForSeason(
    season: SeasonWithEpisodes.Hosted,
    tvShow: TvShow.Hosted,
    query: EpisodeKey.Tmdb
): TulipCompleteEpisodeInfo.Hosted? {
    return season.episodes
        .firstOrNull { it.episodeNumber == query.episodeNumber }
        ?.let { TulipCompleteEpisodeInfo.Hosted(tvShow, season.season, it) }
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

private fun <T> Flow<List<NetworkResult<T>>>.mapNetworkResultToResultInListFlow(): Flow<List<Result<T>>> {
    return map { it.map { networkResult -> networkResult.toResult() } }
}