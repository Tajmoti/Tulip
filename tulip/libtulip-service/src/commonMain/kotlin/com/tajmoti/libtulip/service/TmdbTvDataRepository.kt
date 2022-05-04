package com.tajmoti.libtulip.service

import com.tajmoti.libtulip.model.info.*
import com.tajmoti.libtulip.model.key.*
import com.tajmoti.libtulip.model.result.NetFlow
import com.tajmoti.libtulip.model.result.NetworkResult
import com.tajmoti.libtulip.model.result.convert
import com.tajmoti.libtulip.model.result.toResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/**
 * Repository of complete TV show and movie information.
 * This includes paths to posters, descriptions, ratings and other additional information.
 */
interface TmdbTvDataRepository {
    /**
     * Finds a [TvShowKey.Tmdb] by the [name] and possibly [firstAirYear] of the TV show.
     * The returned flow may never complete, and it may emit an updated value at any time!
     */
    fun findTvShowKey(name: String, firstAirYear: Int?): NetFlow<TvShowKey.Tmdb?>

    /**
     * Finds a [MovieKey.Tmdb] by the [name] and possibly [firstAirYear] of the movie.
     * The returned flow may never complete, and it may emit an updated value at any time!
     */
    fun findMovieKey(name: String, firstAirYear: Int?): NetFlow<MovieKey.Tmdb?>

    /**
     * Returns info about a TV show by its [key].
     * The returned flow may never complete, and it may emit an updated value at any time!
     */
    fun getTvShow(key: TvShowKey.Tmdb): NetFlow<TvShow.Tmdb>

    /**
     * Retrieves information about a TV show episode on a specific streaming site by its [key].
     * The returned flow may never complete, and it may emit an updated value at any time!
     */
    fun getEpisode(key: EpisodeKey.Tmdb): Flow<NetworkResult<Episode.Tmdb>> {
        return getSeasonWithEpisodes(key.seasonKey)
            .map { result -> result.convert { season -> season.episodes.firstOrNull { episode -> episode.key == key } } }
    }

    /**
     * Returns info about a TV show season by its [key].
     * The returned flow may never complete, and it may emit an updated value at any time!
     */
    fun getSeasonWithEpisodes(key: SeasonKey.Tmdb): NetFlow<SeasonWithEpisodes.Tmdb>

    /**
     * Retrieves complete information about a TV show episode by its [key].
     * The returned flow may never complete, and it may emit an updated value at any time!
     */
    fun getFullEpisodeData(key: EpisodeKey.Tmdb): Flow<Result<TulipCompleteEpisodeInfo.Tmdb>> {
        return combine(getTvShow(key.tvShowKey), getEpisode(key)) { tv, episode ->
            episode.convert { ep ->
                tv.convert { tv ->
                    tv.seasons
                        .firstOrNull { it.key == ep.key.seasonKey }
                        ?.let { TulipCompleteEpisodeInfo.Tmdb(tv, it, ep) }
                }.data
            }.toResult()
        }
    }

    /**
     * Returns complete info about a movie by its [key].
     * The returned flow may never complete, and it may emit an updated value at any time!
     */
    fun getMovie(key: MovieKey.Tmdb): NetFlow<TulipMovie.Tmdb>

    /**
     * Returns complete info about either a movie, or a TV show by its [key].
     * The returned flow may never complete, and it may emit an updated value at any time!
     */
    fun getItem(key: ItemKey.Tmdb): NetFlow<out TulipItem.Tmdb> {
        return when (key) {
            is TvShowKey.Tmdb -> getTvShow(key)
            is MovieKey.Tmdb -> getMovie(key)
        }
    }
}