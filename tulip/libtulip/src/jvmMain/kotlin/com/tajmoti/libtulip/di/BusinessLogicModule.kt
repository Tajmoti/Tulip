package com.tajmoti.libtulip.di

import com.tajmoti.commonutils.PageSourceLoader
import com.tajmoti.libopensubtitles.OpenSubtitlesFallbackService
import com.tajmoti.libopensubtitles.OpenSubtitlesService
import com.tajmoti.libtulip.di.impl.BusinessLogicModuleImpl
import com.tajmoti.libtulip.di.qualifier.RawHttpClient
import com.tajmoti.libtulip.facade.*
import com.tajmoti.libtulip.model.hosted.StreamingService
import com.tajmoti.libtulip.repository.UserFavoriteRepository
import com.tajmoti.libtulip.repository.UserPlayingProgressRepository
import com.tajmoti.libtulip.service.HostedTvDataRepository
import com.tajmoti.libtulip.service.ItemMappingRepository
import com.tajmoti.libtulip.service.PlayingHistoryRepository
import com.tajmoti.libtulip.service.TmdbTvDataRepository
import com.tajmoti.libtvprovider.MultiTvProvider
import com.tajmoti.libtvvideoextractor.VideoLinkExtractor
import dagger.Module
import dagger.Provides
import io.ktor.client.*
import javax.inject.Singleton

@Module
object BusinessLogicModule : IBusinessLogicModule {

    // Not used for the time being
    override fun provideSubtitleService(
        openSubtitlesFallbackService: OpenSubtitlesFallbackService
    ) = BusinessLogicModuleImpl.provideSubtitleService(openSubtitlesFallbackService)

    @Provides
    @Singleton
    override fun provideSubtitleFacade(
        openSubtitlesService: OpenSubtitlesService,
        openSubtitlesFallbackService: OpenSubtitlesFallbackService
    ): SubtitleFacade {
        return BusinessLogicModuleImpl.provideSubtitleFacade(openSubtitlesService, openSubtitlesFallbackService)
    }

    override fun provideMultiTvProvider(
        loader: PageSourceLoader,
        blacklist: Set<StreamingService>
    ): MultiTvProvider<StreamingService> {
        return BusinessLogicModuleImpl.provideMultiTvProvider(loader, blacklist)
    }

    @Provides
    @Singleton
    fun provideMultiTvProvider(loader: PageSourceLoader): MultiTvProvider<StreamingService> {
        return provideMultiTvProvider(loader, emptySet())
    }

    @Provides
    @Singleton
    override fun provideLinkExtractor(loader: PageSourceLoader) =
        BusinessLogicModuleImpl.provideLinkExtractor(loader)

    @Provides
    @Singleton
    override fun provideMappingSearchService(
        tvProvider: MultiTvProvider<StreamingService>,
        tmdbRepository: TmdbTvDataRepository,
        itemMappingRepository: ItemMappingRepository
    ): SearchFacade {
        return BusinessLogicModuleImpl.provideMappingSearchService(tvProvider, tmdbRepository, itemMappingRepository)
    }

    @Provides
    @Singleton
    override fun provideTvShowInfoFacade(
        hostedTvDataRepository: HostedTvDataRepository,
        tmdbRepo: TmdbTvDataRepository,
        favoritesRepository: UserFavoriteRepository
    ): TvShowInfoFacade {
        return BusinessLogicModuleImpl.provideTvShowInfoFacade(hostedTvDataRepository, tmdbRepo, favoritesRepository)
    }

    @Provides
    @Singleton
    override fun provideUserFavoriteFacade(
        favoritesRepository: UserFavoriteRepository,
        tmdbRepo: TmdbTvDataRepository,
        hostedTvDataRepository: HostedTvDataRepository,
        historyRepository: PlayingHistoryRepository
    ): UserFavoriteFacade {
        return BusinessLogicModuleImpl.provideUserFavoriteFacade(
            favoritesRepository,
            tmdbRepo,
            hostedTvDataRepository,
            historyRepository
        )
    }

    @Provides
    @Singleton
    override fun providePlayingProgressFacade(repository: UserPlayingProgressRepository): PlayingProgressFacade {
        return BusinessLogicModuleImpl.providePlayingProgressFacade(repository)
    }

    @Provides
    @Singleton
    override fun provideStreamFacade(
        hostedTvDataRepository: HostedTvDataRepository,
        hostedToTmdbMappingRepository: ItemMappingRepository,
        linkExtractor: VideoLinkExtractor,
        @RawHttpClient
        httpClient: HttpClient
    ): StreamFacade {
        return BusinessLogicModuleImpl.provideStreamFacade(
            hostedTvDataRepository,
            hostedToTmdbMappingRepository,
            linkExtractor,
            httpClient
        )
    }
}