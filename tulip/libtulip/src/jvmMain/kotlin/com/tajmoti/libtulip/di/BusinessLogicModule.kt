package com.tajmoti.libtulip.di

import com.tajmoti.commonutils.PageSourceLoader
import com.tajmoti.libopensubtitles.OpenSubtitlesFallbackService
import com.tajmoti.libtulip.di.impl.BusinessLogicModuleImpl
import com.tajmoti.libtulip.model.hosted.StreamingService
import com.tajmoti.libtulip.repository.HostedTvDataRepository
import com.tajmoti.libtulip.repository.ItemMappingRepository
import com.tajmoti.libtulip.repository.TmdbTvDataRepository
import com.tajmoti.libtulip.service.StreamExtractionService
import com.tajmoti.libtvprovider.MultiTvProvider
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object BusinessLogicModule : IBusinessLogicModule {
    @Provides
    @Singleton
    override fun provideStreamService(
        hostedTvDataRepository: HostedTvDataRepository,
        extractionService: StreamExtractionService,
        hostedToTmdbMappingRepository: ItemMappingRepository
    ) = BusinessLogicModuleImpl.provideStreamService(
        hostedTvDataRepository,
        extractionService,
        hostedToTmdbMappingRepository
    )

    @Provides
    @Singleton
    override fun provideSubtitleService(
        openSubtitlesFallbackService: OpenSubtitlesFallbackService
    ) = BusinessLogicModuleImpl.provideSubtitleService(openSubtitlesFallbackService)

    @Provides
    @Singleton
    override fun provideMappingSearchService(
        hostedRepository: HostedTvDataRepository,
        tmdbRepository: TmdbTvDataRepository,
        hostedToTmdbMappingRepository: ItemMappingRepository,
    ) = BusinessLogicModuleImpl.provideMappingSearchService(
        hostedRepository,
        tmdbRepository,
        hostedToTmdbMappingRepository
    )

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
}