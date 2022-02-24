package com.tajmoti.libtulip.repository.impl

import com.tajmoti.libtulip.data.HostedInfoDataSource
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.repository.ItemMappingRepository
import kotlinx.coroutines.flow.Flow

class ItemMappingRepositoryImpl(
    private val hostedInfoDataSource: HostedInfoDataSource
) : ItemMappingRepository {

    override suspend fun createTmdbMappingTv(tmdbKey: TvShowKey.Tmdb, hostedKey: TvShowKey.Hosted) {
        hostedInfoDataSource.createTmdbTvMapping(hostedKey, tmdbKey)
    }

    override suspend fun createTmdbMappingMovie(tmdbKey: MovieKey.Tmdb, hostedKey: MovieKey.Hosted) {
        hostedInfoDataSource.createTmdbMovieMapping(hostedKey, tmdbKey)
    }

    override fun getHostedTvShowKeysByTmdbKey(key: TvShowKey.Tmdb): Flow<Set<TvShowKey.Hosted>> {
        return hostedInfoDataSource.getTmdbMappingForTvShow(key)
    }

    override fun getHostedMovieKeysByTmdbKey(key: MovieKey.Tmdb): Flow<Set<MovieKey.Hosted>> {
        return hostedInfoDataSource.getTmdbMappingForMovie(key)
    }
}