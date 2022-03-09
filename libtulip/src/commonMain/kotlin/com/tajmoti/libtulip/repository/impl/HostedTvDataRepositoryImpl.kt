package com.tajmoti.libtulip.repository.impl

import com.tajmoti.commonutils.LibraryDispatchers
import com.tajmoti.commonutils.flatMap
import com.tajmoti.commonutils.logger
import com.tajmoti.commonutils.mapWithContext
import com.tajmoti.libtulip.TulipConfiguration
import com.tajmoti.libtulip.data.HostedInfoDataSource
import com.tajmoti.libtulip.misc.job.NetFlow
import com.tajmoti.libtulip.misc.job.NetworkResult
import com.tajmoti.libtulip.model.hosted.StreamingService
import com.tajmoti.libtulip.model.info.TulipMovie
import com.tajmoti.libtulip.model.info.TulipTvShowInfo
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.StreamableKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.repository.HostedTvDataRepository
import com.tajmoti.libtulip.repository.TmdbTvDataRepository
import com.tajmoti.libtvprovider.MultiTvProvider
import com.tajmoti.libtvprovider.model.SearchResult
import com.tajmoti.libtvprovider.model.TvItem
import com.tajmoti.libtvprovider.model.VideoStreamRef
import com.tajmoti.multiplatform.store.TStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class HostedTvDataRepositoryImpl(
    private val hostedTvDataRepo: HostedInfoDataSource,
    private val tvProvider: MultiTvProvider<StreamingService>,
    private val tmdbRepo: TmdbTvDataRepository,
    config: TulipConfiguration
) : HostedTvDataRepository {
    private val tvShowStore = TStoreFactory.createStore(
        cache = config.hostedItemCacheParams,
        source = ::fetchTvShow,
        reader = hostedTvDataRepo::getTvShowByKey,
        writer = { _, it -> hostedTvDataRepo.insertTvShow(it) }
    )
    private val movieStore = TStoreFactory.createStore(
        cache = config.hostedItemCacheParams,
        source = ::fetchMovie,
        reader = hostedTvDataRepo::getMovieByKey,
        writer = { _, it -> hostedTvDataRepo.insertMovie(it) }
    )
    private val streamsStore = TStoreFactory.createStore(
        cache = config.hostedItemCacheParams,
        source = ::getStreamableLinksAsFlow
    )


    override fun search(query: String): Flow<Map<StreamingService, Result<List<SearchResult>>>> {
        logger.debug { "Searching '$query'" }
        return tvProvider.search(query)
            .onEach { it.onEach { (service, listResult) -> logExceptions(service, listResult) } }
    }

    override fun getTvShow(key: TvShowKey.Hosted): NetFlow<TulipTvShowInfo.Hosted> {
        logger.debug { "Retrieving $key" }
        return tvShowStore.stream(key)
    }

    private fun fetchTvShow(key: TvShowKey.Hosted): Flow<Result<TulipTvShowInfo.Hosted>> {
        val netFlow = flow { emit(tvProvider.getShow(key.streamingService, key.id)) }
        return netFlow
            .flatMapLatest { tvShowInfoResult ->
                tvShowInfoResult
                    .onFailure { logger.warn(it) { "Failed to fetch TV show for $key" } }
                    .fold({ getHostedShowTvInfo(it, key) }, { flowOf(Result.failure(it)) })
            }
    }

    private fun getHostedShowTvInfo(
        tvShowInfo: TvItem.TvShow,
        key: TvShowKey.Hosted
    ): Flow<Result<TulipTvShowInfo.Hosted>> {
        return tmdbRepo.findTvShowKey(tvShowInfo.info.name, tvShowInfo.info.firstAirDateYear)
            .mapWithContext(LibraryDispatchers.libraryContext) { mapTmdbOrRecover(it.toResult(), tvShowInfo, key) }
    }

    private fun mapTmdbOrRecover(
        result: Result<TvShowKey.Tmdb?>,
        tvShowInfo: TvItem.TvShow,
        key: TvShowKey.Hosted
    ): Result<TulipTvShowInfo.Hosted> {
        return result
            .map { tmdbKey -> tvShowInfo.fromNetwork(key, tmdbKey) }
            .recover { tvShowInfo.fromNetwork(key, null) }
    }

    override fun getMovie(key: MovieKey.Hosted): Flow<NetworkResult<TulipMovie.Hosted>> {
        logger.debug { "Retrieving $key" }
        return movieStore.stream(key)
    }

    private fun fetchMovie(key: MovieKey.Hosted): Flow<Result<TulipMovie.Hosted>> {
        val netFlow = flow { emit(tvProvider.getMovie(key.streamingService, key.id)) }
        return netFlow
            .flatMapLatest { tvShowInfoResult ->
                tvShowInfoResult.fold({ getHostedMovieInfo(it, key) }, { flowOf(Result.failure(it)) })
            }
    }

    private fun getHostedMovieInfo(tvShowInfo: TvItem.Movie, key: MovieKey.Hosted) =
        tmdbRepo.findMovieKey(tvShowInfo.info.name, tvShowInfo.info.firstAirDateYear)
            .mapWithContext(LibraryDispatchers.libraryContext) {
                it.toResult().flatMap { tmdbKey -> Result.success(tvShowInfo.fromNetwork(key, tmdbKey)) }
            }

    private fun logExceptions(service: StreamingService, result: Result<List<SearchResult>>) {
        val exception = result.exceptionOrNull() ?: return
        logger.warn(exception) { "$service failed" }
    }

    override fun fetchStreams(key: StreamableKey.Hosted): NetFlow<List<VideoStreamRef>> {
        logger.debug { "Retrieving $key" }
        return streamsStore.stream(key)
    }

    private fun getStreamableLinksAsFlow(it: StreamableKey.Hosted): Flow<Result<List<VideoStreamRef>>> {
        logger.debug { "Getting streamable links by $it" }
        return flow { emit(tvProvider.getStreamableLinks(it.streamingService, it.id)) }
    }
}