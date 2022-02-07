package com.tajmoti.libtulip.repository.impl

import com.dropbox.android.external.store4.Fetcher
import com.dropbox.android.external.store4.SourceOfTruth
import com.dropbox.android.external.store4.StoreBuilder
import com.dropbox.android.external.store4.StoreRequest
import com.tajmoti.commonutils.LibraryDispatchers
import com.tajmoti.commonutils.flatMap
import com.tajmoti.commonutils.logger
import com.tajmoti.commonutils.mapWithContext
import com.tajmoti.libtulip.TulipConfiguration
import com.tajmoti.libtulip.data.HostedInfoDataSource
import com.tajmoti.libtulip.misc.job.*
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
    private val tvShowStore = StoreBuilder
        .from(
            Fetcher.ofResultFlow { key -> fetchTvShow(key).mapWithContext(LibraryDispatchers.libraryContext) { it.toFetcherResult() } },
            SourceOfTruth.of(
                hostedTvDataRepo::getTvShowByKey,
                { _, it -> hostedTvDataRepo.insertTvShow(it) },
            )
        )
        .cachePolicy(createCache(config.hostedItemCacheParams))
        .build()
    private val movieStore = StoreBuilder
        .from(
            Fetcher.ofResultFlow { key -> fetchMovie(key).mapWithContext(LibraryDispatchers.libraryContext) { it.toFetcherResult() } },
            SourceOfTruth.of(
                hostedTvDataRepo::getMovieByKey,
                { _, it -> hostedTvDataRepo.insertMovie(it) },
            )
        )
        .cachePolicy(createCache(config.hostedItemCacheParams))
        .build()
    private val streamsStore = StoreBuilder
        .from<StreamableKey.Hosted, List<VideoStreamRef>>(
            Fetcher.ofResult { tvProvider.getStreamableLinks(it.streamingService, it.id).toFetcherResult() },
        )
        .cachePolicy(createCache(config.streamCacheParams))
        .build()


    override fun search(query: String): Flow<Map<StreamingService, Result<List<SearchResult>>>> {
        logger.debug { "Searching '$query'" }
        return tvProvider.search(query)
            .onEach { it.onEach { (service, listResult) -> logExceptions(service, listResult) } }
    }

    override fun getTvShow(key: TvShowKey.Hosted): NetFlow<TulipTvShowInfo.Hosted> {
        logger.debug { "Retrieving $key" }
        return tvShowStore.stream(StoreRequest.cached(key, false)).toNetFlow()
    }

    private fun fetchTvShow(key: TvShowKey.Hosted): Flow<Result<TulipTvShowInfo.Hosted>> {
        val netFlow = flow { emit(tvProvider.getShow(key.streamingService, key.id)) }
        return netFlow
            .flatMapLatest { tvShowInfoResult ->
                tvShowInfoResult.fold({ getHostedShowTvInfo(it, key) }, { flowOf(Result.failure(it)) })
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
        return movieStore.stream(StoreRequest.cached(key, false)).toNetFlow()
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
        logger.warn { "$service failed with $exception" }
    }

    override fun fetchStreams(key: StreamableKey.Hosted): NetFlow<List<VideoStreamRef>> {
        logger.debug { "Retrieving $key" }
        return streamsStore.stream(StoreRequest.cached(key, false)).toNetFlow()
    }
}