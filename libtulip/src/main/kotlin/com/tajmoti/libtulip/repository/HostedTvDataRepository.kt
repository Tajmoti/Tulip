package com.tajmoti.libtulip.repository

import com.tajmoti.libtulip.misc.job.NetFlow
import com.tajmoti.libtulip.misc.job.NetworkResult
import com.tajmoti.libtulip.model.hosted.StreamingService
import com.tajmoti.libtulip.model.info.TulipMovie
import com.tajmoti.libtulip.model.info.TulipTvShowInfo
import com.tajmoti.libtulip.model.key.*
import com.tajmoti.libtvprovider.model.SearchResult
import com.tajmoti.libtvprovider.model.VideoStreamRef
import kotlinx.coroutines.flow.Flow

/**
 * Repository of TV show and movie information from specific streaming sites.
 * This includes the ability to search TV shows and movies and retrieving of video links for episodes and movies.
 */
interface HostedTvDataRepository {
    /**
     * Searches [query] on all supported streaming services and returns a flow that emits the search results.
     * The flow will emit multiple values as more search results are loaded in.
     */
    fun search(query: String): Flow<Map<StreamingService, Result<List<SearchResult>>>>


    /**
     * Retrieves information about a TV show on a specific streaming site by its [key].
     * The returned flow may never complete, and it may emit an updated value at any time!
     */
    fun getTvShow(key: TvShowKey.Hosted): Flow<NetworkResult<TulipTvShowInfo.Hosted>>

    /**
     * Retrieves information about a movie on a specific streaming site by its [key].
     * The returned flow may never complete, and it may emit an updated value at any time!
     */
    fun getMovie(key: MovieKey.Hosted): Flow<NetworkResult<TulipMovie.Hosted>>


    /**
     * Retrieves a list of video streams for the provided [key] (either [EpisodeKey.Hosted] or [MovieKey.Hosted]).
     * The flow will emit multiple values as more streams results are loaded in.
     */
    fun fetchStreams(key: StreamableKey.Hosted): NetFlow<List<VideoStreamRef>>
}