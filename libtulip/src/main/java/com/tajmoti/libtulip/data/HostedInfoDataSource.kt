package com.tajmoti.libtulip.data

import com.tajmoti.libtulip.model.info.*
import com.tajmoti.libtulip.model.key.*
import com.tajmoti.libtulip.model.tmdb.TmdbItemId

interface HostedInfoDataSource {

    suspend fun getItemByKey(key: ItemKey.Hosted): TulipItem.Hosted? {
        return when (key) {
            is TvShowKey.Hosted -> getTvShowByKey(key)
            is MovieKey.Hosted -> getMovieByKey(key)
        }
    }


    suspend fun getTvShowByKey(key: TvShowKey.Hosted): TulipTvShowInfo.Hosted?

    suspend fun getTvShowsByTmdbId(key: TvShowKey.Tmdb): List<TulipTvShowInfo.Hosted>

    suspend fun insertTvShow(show: TulipTvShowInfo.Hosted)


    suspend fun getSeasonsByTvShow(key: TvShowKey.Hosted): List<TulipSeasonInfo.Hosted>

    suspend fun getSeasonByKey(key: SeasonKey.Hosted): TulipSeasonInfo.Hosted?


    suspend fun getEpisodesBySeason(key: SeasonKey.Hosted): List<TulipEpisodeInfo.Hosted>

    suspend fun getEpisodeByKey(key: EpisodeKey.Hosted): TulipEpisodeInfo.Hosted?

    suspend fun getEpisodeByTmdbId(key: EpisodeKey.Tmdb): List<TulipEpisodeInfo.Hosted>


    suspend fun getMovieByKey(key: MovieKey.Hosted): TulipMovie.Hosted?

    suspend fun getMovieByTmdbKey(key: MovieKey.Tmdb): List<TulipMovie.Hosted>

    suspend fun insertMovie(movie: TulipMovie.Hosted)


    suspend fun createTmdbMapping(hosted: ItemKey.Hosted, tmdb: TmdbItemId)

    suspend fun getTmdbMappingForTvShow(tmdb: TmdbItemId.Tv): List<TvShowKey.Hosted>

    suspend fun getTmdbMappingForMovie(tmdb: TmdbItemId.Movie): List<MovieKey.Hosted>
}
