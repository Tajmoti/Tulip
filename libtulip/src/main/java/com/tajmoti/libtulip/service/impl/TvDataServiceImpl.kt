package com.tajmoti.libtulip.service.impl

import com.tajmoti.commonutils.logger
import com.tajmoti.commonutils.mapToAsyncJobs
import com.tajmoti.libtmdb.model.tv.Season
import com.tajmoti.libtmdb.model.tv.Tv
import com.tajmoti.libtulip.misc.NetworkResult
import com.tajmoti.libtulip.model.MissingEntityException
import com.tajmoti.libtulip.model.hosted.toKey
import com.tajmoti.libtulip.model.info.StreamableInfo
import com.tajmoti.libtulip.model.info.TulipEpisodeInfo
import com.tajmoti.libtulip.model.key.*
import com.tajmoti.libtulip.model.tmdb.TmdbCompleteTvShow
import com.tajmoti.libtulip.model.tmdb.TmdbItemId
import com.tajmoti.libtulip.repository.TmdbTvDataRepository
import com.tajmoti.libtulip.service.TvDataService
import com.tajmoti.libtulip.service.takeIfNoneNull
import com.tajmoti.libtvprovider.SearchResult
import com.tajmoti.libtvprovider.TvItemInfo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TvDataServiceImpl @Inject constructor(
    private val tvDataRepo: TmdbTvDataRepository
) : TvDataService {

    override suspend fun prefetchTvShowData(key: TvShowKey.Tmdb): Result<Unit> {
        logger.debug("Prefetching TMDB data for $key")
        val tv = tvDataRepo.getTv(key)
            ?: return Result.failure(NullPointerException())
        mapToAsyncJobs(tv.seasons) { tvDataRepo.getSeason(it.toKey(tv)) }
            .takeIfNoneNull() ?: return Result.failure(NullPointerException())
        logger.debug("Prefetching TMDB data for $key successful")
        return Result.success(Unit)
    }

    override suspend fun getTvShow(key: TvShowKey.Tmdb): Result<Tv> {
        return tvDataRepo.getTv(key)
            ?.let { Result.success(it) }
            ?: Result.failure(MissingEntityException)
    }

    override suspend fun getTvShowAsFlow(key: TvShowKey.Tmdb): Flow<NetworkResult<Tv>> {
        return tvDataRepo.getTvAsFlow(key)
    }

    override suspend fun getTvShowWithSeasonsAsFlow(key: TvShowKey.Tmdb): Flow<NetworkResult<TmdbCompleteTvShow>> {
        return tvDataRepo.getTvShowWithSeasonsAsFlow(key)
    }

    override suspend fun getSeason(key: SeasonKey.Tmdb): Result<Season> {
        return tvDataRepo.getSeason(key)
            ?.let { Result.success(it) }
            ?: Result.failure(MissingEntityException)
    }

    override suspend fun getStreamableInfo(key: StreamableKey.Tmdb): Result<StreamableInfo> {
        return when (key) {
            is EpisodeKey.Tmdb -> getEpisodeInfo(key)
            is MovieKey.Tmdb -> getMovieInfo(key)
        }
    }

    private suspend fun getMovieInfo(key: MovieKey.Tmdb): Result<StreamableInfo.Movie> {
        val name = tvDataRepo.getMovie(key.id)
            ?: return Result.failure(MissingEntityException)
        val movie = StreamableInfo.Movie(name.title)
        return Result.success(movie)
    }

    private suspend fun getEpisodeInfo(key: EpisodeKey.Tmdb): Result<StreamableInfo.Episode> {
        val result = tvDataRepo.getFullEpisodeData(key)
            ?: return Result.failure(MissingEntityException)
        val (sh, ss, ep) = result
        val episode = StreamableInfo.Episode(
            showName = sh.name,
            seasonNumber = ss.seasonNumber,
            info = TulipEpisodeInfo(ep.episodeNumber, ep.name)
        )
        return Result.success(episode)
    }

    override suspend fun findTmdbId(type: SearchResult.Type, info: TvItemInfo): TmdbItemId? {
        return try {
            when (type) {
                SearchResult.Type.TV_SHOW -> tvDataRepo.searchTv(
                    info.name,
                    info.firstAirDateYear
                )
                    .getOrNull()
                    ?.results
                    ?.firstOrNull()
                    ?.id
                    ?.let { TmdbItemId.Tv(it) }
                SearchResult.Type.MOVIE -> tvDataRepo.searchMovie(
                    info.name,
                    info.firstAirDateYear
                )
                    .getOrNull()
                    ?.results
                    ?.firstOrNull()
                    ?.id
                    ?.let { TmdbItemId.Movie(it) }
            }
        } catch (e: Throwable) {
            logger.warn("Exception while searching IMDB id for {}", info, e)
            null
        }
    }
}