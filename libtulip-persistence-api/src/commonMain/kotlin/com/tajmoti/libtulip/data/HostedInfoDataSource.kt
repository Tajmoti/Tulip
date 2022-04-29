package com.tajmoti.libtulip.data

import com.tajmoti.libtulip.model.info.SeasonWithEpisodes
import com.tajmoti.libtulip.model.info.TulipMovie
import com.tajmoti.libtulip.model.info.TvShow
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.model.key.TvShowKey
import kotlinx.coroutines.flow.Flow

interface HostedInfoDataSource {
    fun getTvShowByKey(key: TvShowKey.Hosted): Flow<TvShow.Hosted?>

    fun getSeasonByKey(key: SeasonKey.Hosted): Flow<SeasonWithEpisodes.Hosted?>

    fun getMovieByKey(key: MovieKey.Hosted): Flow<TulipMovie.Hosted?>

    suspend fun insertTvShow(show: TvShow.Hosted)

    suspend fun insertSeasons(seasons: List<SeasonWithEpisodes.Hosted>)

    suspend fun insertMovie(movie: TulipMovie.Hosted)


    suspend fun createTmdbTvMapping(hosted: TvShowKey.Hosted, tmdb: TvShowKey.Tmdb)

    suspend fun createTmdbMovieMapping(hosted: MovieKey.Hosted, tmdb: MovieKey.Tmdb)

    fun getTmdbMappingForTvShow(tmdb: TvShowKey.Tmdb): Flow<Set<TvShowKey.Hosted>>

    fun getTmdbMappingForMovie(tmdb: MovieKey.Tmdb): Flow<Set<MovieKey.Hosted>>
}
