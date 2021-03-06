package com.tajmoti.libtulip.repository.impl

import arrow.core.Option
import arrow.core.toOption
import com.tajmoti.commonutils.logger
import com.tajmoti.libtulip.TulipConfiguration
import com.tajmoti.libtulip.model.SeasonWithEpisodes
import com.tajmoti.libtulip.model.TulipMovie
import com.tajmoti.libtulip.model.TvShow
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.model.result.NetFlow
import com.tajmoti.libtulip.model.result.NetworkResult
import com.tajmoti.libtulip.model.result.convert
import com.tajmoti.libtulip.model.result.toResult
import com.tajmoti.libtulip.repository.TmdbMovieRepository
import com.tajmoti.libtulip.repository.TmdbSeasonRepository
import com.tajmoti.libtulip.repository.TmdbTvShowRepository
import com.tajmoti.libtulip.service.TmdbTvDataRepository
import com.tajmoti.multiplatform.store.TStoreFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CachingTvDataRepository(
    private val net: TmdbTvDataRepository,
    private val tvRepository: TmdbTvShowRepository,
    private val seasonRepository: TmdbSeasonRepository,
    private val movieRepository: TmdbMovieRepository,
    config: TulipConfiguration.CacheParameters
) : TmdbTvDataRepository {
    private val tvStore = TStoreFactory.createStore(
        cache = config,
        source = { key -> net.getTvShow(key).map { it.toResult() } },
        reader = tvRepository::findByKey,
        writer = { _, it -> tvRepository.insert(it) }
    )
    private val seasonStore = TStoreFactory.createStore(
        cache = config,
        source = { key -> net.getSeasonWithEpisodes(key).map { it.toResult() } },
        reader = seasonRepository::findSeasonWithEpisodesByKey,
        writer = { _, it -> seasonRepository.insertSeasonWithEpisodes(it.season, it.episodes) }
    )
    private val movieStore = TStoreFactory.createStore(
        cache = config,
        source = { key -> net.getMovie(key).map { it.toResult() } },
        reader = movieRepository::findByKey,
        writer = { _, it -> movieRepository.insert(it) },
    )
    private val tmdbTvIdStore = TStoreFactory.createStore<TitleQuery, Option<TvShowKey.Tmdb>>(
        cache = config,
        source = {
            net.findTvShowKey(it.name, it.firstAirDate).map { net -> net.toResult().map { key -> key.toOption() } }
        },
    )
    private val tmdbMovieIdStore = TStoreFactory.createStore<TitleQuery, Option<MovieKey.Tmdb>>(
        cache = config,
        source = {
            net.findMovieKey(it.name, it.firstAirDate).map { net -> net.toResult().map { key -> key.toOption() } }
        },
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