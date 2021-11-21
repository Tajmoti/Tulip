package com.tajmoti.libtulip.repository

import com.tajmoti.libtulip.misc.job.NetFlow
import com.tajmoti.libtulip.model.info.TulipMovie
import com.tajmoti.libtulip.model.info.TulipTvShowInfo
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.model.tmdb.TmdbItemId

interface TmdbTvDataRepository {
    fun findTmdbIdTv(name: String, firstAirYear: Int?): NetFlow<TmdbItemId.Tv?>

    fun findTmdbIdMovie(name: String, firstAirYear: Int?): NetFlow<TmdbItemId.Movie?>

    fun getTvShow(key: TvShowKey.Tmdb): NetFlow<TulipTvShowInfo.Tmdb>

    fun getMovie(key: MovieKey.Tmdb): NetFlow<TulipMovie.Tmdb>
}