package com.tajmoti.libtulip.repository

import com.tajmoti.libtulip.misc.job.NetFlow
import com.tajmoti.libtulip.model.info.TulipItem
import com.tajmoti.libtulip.model.info.TulipMovie
import com.tajmoti.libtulip.model.info.TulipTvShowInfo
import com.tajmoti.libtulip.model.key.ItemKey
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.TvShowKey

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
     * Returns complete info about a TV show by its [key].
     * The returned flow may never complete, and it may emit an updated value at any time!
     */
    fun getTvShow(key: TvShowKey.Tmdb): NetFlow<TulipTvShowInfo.Tmdb>

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