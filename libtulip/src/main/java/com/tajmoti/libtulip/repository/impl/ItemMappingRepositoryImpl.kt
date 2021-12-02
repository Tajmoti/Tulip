package com.tajmoti.libtulip.repository.impl

import com.tajmoti.libtulip.data.HostedInfoDataSource
import com.tajmoti.libtulip.model.key.ItemKey
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.repository.ItemMappingRepository
import kotlinx.coroutines.flow.Flow

class ItemMappingRepositoryImpl(
    private val hostedInfoDataSource: HostedInfoDataSource
) : ItemMappingRepository {
    override suspend fun createTmdbMapping(tmdbKey: ItemKey.Tmdb, hostedKey: ItemKey.Hosted) {
        when (tmdbKey) {
            is TvShowKey.Tmdb -> hostedInfoDataSource.createTmdbMapping(hostedKey as TvShowKey.Hosted, tmdbKey)
            is MovieKey.Tmdb -> hostedInfoDataSource.createTmdbMapping(hostedKey as MovieKey.Hosted, tmdbKey)
        }
    }

    override fun getHostedTvShowKeysByTmdbKey(key: TvShowKey.Tmdb): Flow<List<TvShowKey.Hosted>> {
        return hostedInfoDataSource.getTmdbMappingForTvShow(key)
    }

    override fun getHostedMovieKeysByTmdbKey(key: MovieKey.Tmdb): Flow<List<MovieKey.Hosted>> {
        return hostedInfoDataSource.getTmdbMappingForMovie(key)
    }
}