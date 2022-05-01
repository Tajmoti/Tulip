package com.tajmoti.libtulip.di

import com.tajmoti.libopensubtitles.OpenSubtitlesFallbackService
import com.tajmoti.libopensubtitles.OpenSubtitlesService
import com.tajmoti.libtmdb.TmdbService
import com.tajmoti.libtulip.TulipConfiguration
import com.tajmoti.libtulip.data.*
import com.tajmoti.libtulip.model.hosted.StreamingService
import com.tajmoti.libtulip.repository.*
import com.tajmoti.libtulip.service.StreamExtractionService
import com.tajmoti.libtvprovider.MultiTvProvider
import com.tajmoti.libtvvideoextractor.VideoLinkExtractor
import io.ktor.client.*

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

    fun provideStreamsRepository(
        linkExtractor: VideoLinkExtractor,
        httpClient: HttpClient
    ): StreamExtractionService

    fun provideTmdbTvDataRepository(
        service: TmdbService,
        tvRepository: TmdbTvShowRepository,
        seasonRepository: TmdbSeasonRepository,
        movieRepository: TmdbMovieRepository,
        config: TulipConfiguration
    ): TmdbTvDataRepository

    fun provideFavoritesRepository(repo: UserFavoriteRepository): FavoritesRepository

    fun provideSubtitleRepository(
        openSubtitlesService: OpenSubtitlesService,
        openSubtitlesFallbackService: OpenSubtitlesFallbackService,
    ): SubtitleRepository

    fun providePlayingHistoryRepository(dataSource: UserLastPlayedPositionRepository): PlayingHistoryRepository
}