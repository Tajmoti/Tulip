package com.tajmoti.libtulip.di

import com.tajmoti.libtmdb.TmdbService
import com.tajmoti.libtulip.TulipConfiguration
import com.tajmoti.libtulip.di.impl.DataRepositoryModuleImpl
import com.tajmoti.libtulip.model.hosted.StreamingService
import com.tajmoti.libtulip.repository.*
import com.tajmoti.libtulip.service.HostedTvDataRepository
import com.tajmoti.libtulip.service.ItemMappingRepository
import com.tajmoti.libtulip.service.PlayingHistoryRepository
import com.tajmoti.libtulip.service.TmdbTvDataRepository
import com.tajmoti.libtvprovider.MultiTvProvider
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object DataRepositoryModule : IDataRepositoryModule {

    @Provides
    @Singleton
    override fun bindHostedTvDataRepository(
        tvRepository: HostedTvShowRepository,
        seasonRepository: HostedSeasonRepository,
        movieRepository: HostedMovieRepository,
        tvProvider: MultiTvProvider<StreamingService>,
        tmdbRepo: TmdbTvDataRepository,
        config: TulipConfiguration
    ): HostedTvDataRepository {
        return DataRepositoryModuleImpl.bindHostedTvDataRepository(
            tvRepository,
            seasonRepository,
            movieRepository,
            tvProvider,
            tmdbRepo,
            config
        )
    }

    @Provides
    @Singleton
    override fun provideItemMappingRepository(
        hostedTvDataRepo: TvShowMappingRepository,
        movieMappingRepository: MovieMappingRepository,
    ): ItemMappingRepository {
        return DataRepositoryModuleImpl.provideItemMappingRepository(hostedTvDataRepo, movieMappingRepository)
    }

    @Provides
    @Singleton
    override fun provideTmdbTvDataRepository(
        service: TmdbService,
        tvRepository: TmdbTvShowRepository,
        seasonRepository: TmdbSeasonRepository,
        movieRepository: TmdbMovieRepository,
        config: TulipConfiguration
    ): TmdbTvDataRepository {
        return DataRepositoryModuleImpl.provideTmdbTvDataRepository(
            service,
            tvRepository,
            seasonRepository,
            movieRepository,
            config
        )
    }

    @Provides
    @Singleton
    override fun providePlayingHistoryRepository(dataSource: UserPlayingProgressRepository): PlayingHistoryRepository {
        return DataRepositoryModuleImpl.providePlayingHistoryRepository(dataSource)
    }
}