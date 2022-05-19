package com.tajmoti.libtulip.di

import com.tajmoti.libtmdb.TmdbService
import com.tajmoti.libtulip.TulipConfiguration
import com.tajmoti.libtulip.model.key.StreamingService
import com.tajmoti.libtulip.repository.*
import com.tajmoti.libtulip.service.HostedTvDataRepository
import com.tajmoti.libtulip.service.ItemMappingRepository
import com.tajmoti.libtulip.service.PlayingHistoryRepository
import com.tajmoti.libtulip.service.TmdbTvDataRepository
import com.tajmoti.libtvprovider.MultiTvProvider

interface IDataRepositoryModule {
    fun bindHostedTvDataRepository(
        tvRepository: HostedTvShowRepository,
        seasonRepository: HostedSeasonRepository,
        movieRepository: HostedMovieRepository,
        tvProvider: MultiTvProvider<StreamingService>,
        tmdbRepo: TmdbTvDataRepository,
        config: TulipConfiguration
    ): HostedTvDataRepository

    fun provideItemMappingRepository(
        hostedTvDataRepo: TvShowMappingRepository,
        movieMappingRepository: MovieMappingRepository,
    ): ItemMappingRepository

    fun provideTmdbTvDataRepository(
        service: TmdbService,
        tvRepository: TmdbTvShowRepository,
        seasonRepository: TmdbSeasonRepository,
        movieRepository: TmdbMovieRepository,
        config: TulipConfiguration
    ): TmdbTvDataRepository

    fun providePlayingHistoryRepository(dataSource: UserPlayingProgressRepository): PlayingHistoryRepository
}