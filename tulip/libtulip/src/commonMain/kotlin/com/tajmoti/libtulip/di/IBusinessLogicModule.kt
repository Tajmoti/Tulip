package com.tajmoti.libtulip.di

import com.tajmoti.commonutils.PageSourceLoader
import com.tajmoti.libopensubtitles.OpenSubtitlesFallbackService
import com.tajmoti.libtulip.model.hosted.StreamingService
import com.tajmoti.libtulip.repository.HostedTvDataRepository
import com.tajmoti.libtulip.repository.ItemMappingRepository
import com.tajmoti.libtulip.repository.TmdbTvDataRepository
import com.tajmoti.libtulip.service.MappingSearchService
import com.tajmoti.libtulip.service.StreamExtractionService
import com.tajmoti.libtulip.service.StreamService
import com.tajmoti.libtulip.service.SubtitleService
import com.tajmoti.libtvprovider.MultiTvProvider
import com.tajmoti.libtvvideoextractor.VideoLinkExtractor

interface IBusinessLogicModule {
    fun provideStreamService(
        hostedTvDataRepository: HostedTvDataRepository,
        extractionService: StreamExtractionService,
        hostedToTmdbMappingRepository: ItemMappingRepository
    ): StreamService

    fun provideSubtitleService(
        openSubtitlesFallbackService: OpenSubtitlesFallbackService
    ): SubtitleService

    fun provideMappingSearchService(
        hostedRepository: HostedTvDataRepository,
        tmdbRepository: TmdbTvDataRepository,
        hostedToTmdbMappingRepository: ItemMappingRepository,
    ): MappingSearchService

    fun provideMultiTvProvider(
        loader: PageSourceLoader,
        blacklist: Set<StreamingService> = emptySet()
    ): MultiTvProvider<StreamingService>

    fun provideLinkExtractor(loader: PageSourceLoader): VideoLinkExtractor
}