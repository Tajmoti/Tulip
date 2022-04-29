package com.tajmoti.libtulip.repository

import com.tajmoti.libtulip.misc.job.NetFlow
import com.tajmoti.libtulip.misc.job.NetworkResult
import com.tajmoti.libtulip.model.hosted.StreamingService
import com.tajmoti.libtulip.model.info.*
import com.tajmoti.libtulip.model.key.*
import com.tajmoti.libtvprovider.model.SearchResult
import com.tajmoti.libtvprovider.model.VideoStreamRef
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

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
    fun getTvShow(key: TvShowKey.Hosted): Flow<NetworkResult<TvShow.Hosted>>

    /**
     * Retrieves information about a TV show season on a specific streaming site by its [key].
     * The returned flow may never complete, and it may emit an updated value at any time!
     */
    fun getSeasonWithEpisodes(key: SeasonKey.Hosted): Flow<NetworkResult<SeasonWithEpisodes.Hosted>>

    /**
     * Retrieves information about a TV show episode on a specific streaming site by its [key].
     * The returned flow may never complete, and it may emit an updated value at any time!
     */
    fun getEpisode(key: EpisodeKey.Hosted): Flow<NetworkResult<Episode.Hosted>> {
        return getSeasonWithEpisodes(key.seasonKey)
            .map { result -> result.convert { season -> season.episodes.firstOrNull { episode -> episode.key == key } } }
    }

    /**
     * Retrieves complete information about a TV show episode on a specific streaming site by its [key].
     * The returned flow may never complete, and it may emit an updated value at any time!
     */
    fun getEpisodeInfo(key: EpisodeKey.Hosted): Flow<Result<TulipCompleteEpisodeInfo.Hosted>> {
        return combine(getTvShow(key.tvShowKey), getEpisode(key)) { tv, episode ->
            episode.convert { ep ->
                tv.convert { tv ->
                    tv.seasons
                        .firstOrNull { it.key == ep.key.seasonKey }
                        ?.let { TulipCompleteEpisodeInfo.Hosted(tv, it, ep) }
                }.data
            }.toResult()
        }
    }

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