package com.tajmoti.libtulip.di

import com.tajmoti.libopensubtitles.OpenSubtitlesFallbackService
import com.tajmoti.libopensubtitles.OpenSubtitlesService
import com.tajmoti.libtmdb.TmdbService
import com.tajmoti.libtulip.TulipConfiguration
import com.tajmoti.libtulip.data.HostedInfoDataSource
import com.tajmoti.libtulip.data.LocalTvDataSource
import com.tajmoti.libtulip.data.UserDataDataSource
import com.tajmoti.libtulip.model.hosted.StreamingService
import com.tajmoti.libtulip.repository.*
import com.tajmoti.libtulip.service.StreamExtractionService
import com.tajmoti.libtvprovider.MultiTvProvider
import com.tajmoti.libtvvideoextractor.VideoLinkExtractor
import io.ktor.client.*

interface IDataRepositoryModule {
    fun bindHostedTvDataRepository(
        hostedTvDataRepo: HostedInfoDataSource,
        tvProvider: MultiTvProvider<StreamingService>,
        tmdbRepo: TmdbTvDataRepository,
        config: TulipConfiguration
    ): HostedTvDataRepository

    fun provideItemMappingRepository(
        hostedTvDataRepo: HostedInfoDataSource
    ): ItemMappingRepository

    fun provideStreamsRepository(
        linkExtractor: VideoLinkExtractor,
        httpClient: HttpClient
    ): StreamExtractionService

    fun provideTmdbTvDataRepository(
        service: TmdbService,
        db: LocalTvDataSource,
        config: TulipConfiguration
    ): TmdbTvDataRepository

    fun provideFavoritesRepository(repo: UserDataDataSource): FavoritesRepository

    fun provideSubtitleRepository(
        openSubtitlesService: OpenSubtitlesService,
        openSubtitlesFallbackService: OpenSubtitlesFallbackService,
    ): SubtitleRepository

    fun providePlayingHistoryRepository(dataSource: UserDataDataSource): PlayingHistoryRepository
}