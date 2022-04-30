package com.tajmoti.libtulip.repository.impl

import arrow.core.Option
import arrow.core.toOption
import com.tajmoti.commonutils.logger
import com.tajmoti.libtulip.TulipConfiguration
import com.tajmoti.libtulip.data.LocalTvDataSource
import com.tajmoti.libtulip.misc.job.NetFlow
import com.tajmoti.libtulip.misc.job.NetworkResult
import com.tajmoti.libtulip.model.info.SeasonWithEpisodes
import com.tajmoti.libtulip.model.info.TulipMovie
import com.tajmoti.libtulip.model.info.TvShow
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.repository.TmdbTvDataRepository
import com.tajmoti.multiplatform.store.TStoreFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CachingTvDataRepository(
    private val net: TmdbTvDataRepository,
    private val db: LocalTvDataSource,
    config: TulipConfiguration.CacheParameters
) : TmdbTvDataRepository {
    private val tvStore = TStoreFactory.createStore(
        cache = config,
        source = { key -> net.getTvShow(key).map { it.toResult() } },
        reader = db::getTvShow,
        writer = { _, it -> db.insertTvShow(it) }
    )
    private val seasonStore = TStoreFactory.createStore(
        cache = config,
        source = { key -> net.getSeasonWithEpisodes(key).map { it.toResult() } },
        reader = db::getSeason,
        writer = { _, it -> db.insertSeason(it) }
    )
    private val movieStore = TStoreFactory.createStore(
        cache = config,
        source = { key -> net.getMovie(key).map { it.toResult() } },
        reader = db::getMovie,
        writer = { _, it -> db.insertMovie(it) },
    )
    private val tmdbTvIdStore = TStoreFactory.createStore<TitleQuery, Option<TvShowKey.Tmdb>>(
        cache = config,
        source = { net.findTvShowKey(it.name, it.firstAirDate).map { net -> net.toResult().map { key -> key.toOption() } } },
    )
    private val tmdbMovieIdStore = TStoreFactory.createStore<TitleQuery, Option<MovieKey.Tmdb>>(
        cache = config,
        source = { net.findMovieKey(it.name, it.firstAirDate).map { net -> net.toResult().map { key -> key.toOption() } } },
    )

    override fun findTvShowKey(name: String, firstAirYear: Int?): Flow<NetworkResult<TvShowKey.Tmdb?>> {
        logger.debug { "Looking up TMDB ID for TV show $name ($firstAirYear)" }
        return tmdbTvIdStore.stream(TitleQuery(name, firstAirYear))
            .map { it.convert { opt -> opt.orNull() } }
    }

    override fun findMovieKey(name: String, firstAirYear: Int?): Flow<NetworkResult<MovieKey.Tmdb?>> {
        logger.debug { "Looking up TMDB ID for movie $name ($firstAirYear)" }
        return tmdbMovieIdStore.stream(TitleQuery(name, firstAirYear))
            .map { it.convert { opt -> opt.orNull() } }
    }

    override fun getTvShow(key: TvShowKey.Tmdb): Flow<NetworkResult<TvShow.Tmdb>> {
        logger.debug { "Retrieving $key" }
        return tvStore.stream(key)
    }

    override fun getSeasonWithEpisodes(key: SeasonKey.Tmdb): NetFlow<SeasonWithEpisodes.Tmdb> {
        logger.debug { "Retrieving $key" }
        return seasonStore.stream(key)
    }

    override fun getMovie(key: MovieKey.Tmdb): Flow<NetworkResult<TulipMovie.Tmdb>> {
        logger.debug { "Retrieving $key" }
        return movieStore.stream(key)
    }

    data class TitleQuery(
        val name: String,
        val firstAirDate: Int?
    )
}