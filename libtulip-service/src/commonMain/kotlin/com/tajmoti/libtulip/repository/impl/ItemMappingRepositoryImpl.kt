package com.tajmoti.libtulip.repository.impl

import com.tajmoti.commonutils.logger
import com.tajmoti.libtulip.data.MovieMappingRepository
import com.tajmoti.libtulip.data.TvShowMappingRepository
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.repository.ItemMappingRepository
import kotlinx.coroutines.flow.Flow

class ItemMappingRepositoryImpl(
    private val tvShowMappingRepository: TvShowMappingRepository,
    private val movieMappingRepository: MovieMappingRepository,
) : ItemMappingRepository {

    override suspend fun createTmdbMappingTv(tmdbKey: TvShowKey.Tmdb, hostedKey: TvShowKey.Hosted) {
        logger.debug { "Creating mapping of $tmdbKey to $hostedKey" }
        tvShowMappingRepository.createTmdbTvMapping(hostedKey, tmdbKey)
    }

    override suspend fun createTmdbMappingMovie(tmdbKey: MovieKey.Tmdb, hostedKey: MovieKey.Hosted) {
        logger.debug { "Creating mapping of $tmdbKey to $hostedKey" }
        movieMappingRepository.createTmdbMovieMapping(hostedKey, tmdbKey)
    }

    override fun getHostedTvShowKeysByTmdbKey(key: TvShowKey.Tmdb): Flow<Set<TvShowKey.Hosted>> {
        logger.debug { "Retrieving mapping for $key" }
        return tvShowMappingRepository.getTmdbMappingForTvShow(key)
    }

    override fun getHostedMovieKeysByTmdbKey(key: MovieKey.Tmdb): Flow<Set<MovieKey.Hosted>> {
        logger.debug { "Retrieving mapping for $key" }
        return movieMappingRepository.getTmdbMappingForMovie(key)
    }
}