package com.tajmoti.libtulip.di

import com.tajmoti.libopensubtitles.OpenSubtitlesFallbackService
import com.tajmoti.libopensubtitles.OpenSubtitlesService
import com.tajmoti.libtmdb.TmdbService
import com.tajmoti.libtulip.TulipConfiguration
import com.tajmoti.libtulip.data.*
import com.tajmoti.libtulip.di.impl.DataRepositoryModuleImpl
import com.tajmoti.libtulip.di.qualifier.RawHttpClient
import com.tajmoti.libtulip.model.hosted.StreamingService
import com.tajmoti.libtulip.repository.*
import com.tajmoti.libtulip.service.StreamExtractionService
import com.tajmoti.libtvprovider.MultiTvProvider
import com.tajmoti.libtvvideoextractor.VideoLinkExtractor
import dagger.Module
import dagger.Provides
import io.ktor.client.*
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
    override fun provideStreamsRepository(
        linkExtractor: VideoLinkExtractor,
        @RawHttpClient
        httpClient: HttpClient
    ): StreamExtractionService {
        return DataRepositoryModuleImpl.provideStreamsRepository(linkExtractor, httpClient)
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
    override fun provideFavoritesRepository(repo: UserFavoriteRepository): FavoritesRepository {
        return DataRepositoryModuleImpl.provideFavoritesRepository(repo)
    }

    @Provides
    @Singleton
    override fun provideSubtitleRepository(
        openSubtitlesService: OpenSubtitlesService,
        openSubtitlesFallbackService: OpenSubtitlesFallbackService,
    ): SubtitleRepository {
        return DataRepositoryModuleImpl.provideSubtitleRepository(openSubtitlesService, openSubtitlesFallbackService)
    }

    @Provides
    @Singleton
    override fun providePlayingHistoryRepository(dataSource: UserLastPlayedPositionRepository): PlayingHistoryRepository {
        return DataRepositoryModuleImpl.providePlayingHistoryRepository(dataSource)
    }
}