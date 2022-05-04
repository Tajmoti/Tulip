package com.tajmoti.libtulip.di.impl

import com.tajmoti.libtmdb.TmdbService
import com.tajmoti.libtulip.TulipConfiguration
import com.tajmoti.libtulip.di.IDataRepositoryModule
import com.tajmoti.libtulip.model.hosted.StreamingService
import com.tajmoti.libtulip.repository.*
import com.tajmoti.libtulip.repository.impl.CachingTvDataRepository
import com.tajmoti.libtulip.repository.impl.HostedTvDataRepositoryImpl
import com.tajmoti.libtulip.repository.impl.ItemMappingRepositoryImpl
import com.tajmoti.libtulip.repository.impl.PlayingHistoryRepositoryImpl
import com.tajmoti.libtulip.service.*
import com.tajmoti.libtvprovider.MultiTvProvider

object DataRepositoryModuleImpl : IDataRepositoryModule {

    override fun bindHostedTvDataRepository(
        tvRepository: HostedTvShowRepository,
        seasonRepository: HostedSeasonRepository,
        movieRepository: HostedMovieRepository,
        tvProvider: MultiTvProvider<StreamingService>,
        tmdbRepo: TmdbTvDataRepository,
        config: TulipConfiguration
    ): HostedTvDataRepository {
        return HostedTvDataRepositoryImpl(tvRepository, seasonRepository, movieRepository, tvProvider, tmdbRepo, config)
    }

    override fun provideItemMappingRepository(
        hostedTvDataRepo: TvShowMappingRepository,
        movieMappingRepository: MovieMappingRepository,
    ): ItemMappingRepository {
        return ItemMappingRepositoryImpl(hostedTvDataRepo, movieMappingRepository)
    }

    override fun provideTmdbTvDataRepository(
        service: TmdbService,
        tvRepository: TmdbTvShowRepository,
        seasonRepository: TmdbSeasonRepository,
        movieRepository: TmdbMovieRepository,
        config: TulipConfiguration
    ): TmdbTvDataRepository {
        return CachingTvDataRepository(
            LibTmdbRepository(service),
            tvRepository,
            seasonRepository,
            movieRepository,
            config.tmdbCacheParams
        )
    }

    override fun providePlayingHistoryRepository(dataSource: UserPlayingProgressRepository): PlayingHistoryRepository {
        return PlayingHistoryRepositoryImpl(dataSource)
    }
}