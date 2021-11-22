package com.tajmoti.libtulip.repository

import com.tajmoti.libtulip.misc.job.NetFlow
import com.tajmoti.libtulip.model.info.TulipMovie
import com.tajmoti.libtulip.model.info.TulipTvShowInfo
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.TvShowKey

interface TmdbTvDataRepository {
    fun findTmdbIdTv(name: String, firstAirYear: Int?): NetFlow<TvShowKey.Tmdb?>

    fun findTmdbIdMovie(name: String, firstAirYear: Int?): NetFlow<MovieKey.Tmdb?>

    fun getTvShow(key: TvShowKey.Tmdb): NetFlow<TulipTvShowInfo.Tmdb>

    fun getMovie(key: MovieKey.Tmdb): NetFlow<TulipMovie.Tmdb>
}