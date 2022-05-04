package com.tajmoti.libtulip.di

import com.tajmoti.commonutils.PageSourceLoader
import com.tajmoti.libopensubtitles.OpenSubtitlesFallbackService
import com.tajmoti.libopensubtitles.OpenSubtitlesService
import com.tajmoti.libtulip.facade.*
import com.tajmoti.libtulip.model.hosted.StreamingService
import com.tajmoti.libtulip.repository.UserFavoriteRepository
import com.tajmoti.libtulip.repository.UserPlayingProgressRepository
import com.tajmoti.libtulip.service.*
import com.tajmoti.libtvprovider.MultiTvProvider
import com.tajmoti.libtvvideoextractor.VideoLinkExtractor
import io.ktor.client.*

interface IBusinessLogicModule {

    fun provideSubtitleService(
        openSubtitlesFallbackService: OpenSubtitlesFallbackService
    ): SubtitleService

    fun provideSubtitleFacade(
        openSubtitlesService: OpenSubtitlesService,
        openSubtitlesFallbackService: OpenSubtitlesFallbackService
    ): SubtitleFacade

    fun provideMappingSearchService(
        tvProvider: MultiTvProvider<StreamingService>,
        tmdbRepository: TmdbTvDataRepository,
        itemMappingRepository: ItemMappingRepository,
    ): SearchFacade

    fun provideMultiTvProvider(
        loader: PageSourceLoader,
        blacklist: Set<StreamingService> = emptySet()
    ): MultiTvProvider<StreamingService>

    fun provideLinkExtractor(loader: PageSourceLoader): VideoLinkExtractor

    fun provideTvShowInfoFacade(
        hostedTvDataRepository: HostedTvDataRepository,
        tmdbRepo: TmdbTvDataRepository,
        favoritesRepository: UserFavoriteRepository
    ): TvShowInfoFacade

    fun provideUserFavoriteFacade(
        favoritesRepository: UserFavoriteRepository,
        tmdbRepo: TmdbTvDataRepository,
        hostedTvDataRepository: HostedTvDataRepository,
        historyRepository: PlayingHistoryRepository,
    ): UserFavoriteFacade

    fun providePlayingProgressFacade(repository: UserPlayingProgressRepository): PlayingProgressFacade

    fun provideStreamFacade(
        hostedTvDataRepository: HostedTvDataRepository,
        hostedToTmdbMappingRepository: ItemMappingRepository,
        linkExtractor: VideoLinkExtractor,
        httpClient: HttpClient
    ): StreamFacade
}