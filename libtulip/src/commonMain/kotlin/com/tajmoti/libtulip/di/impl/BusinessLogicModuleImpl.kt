package com.tajmoti.libtulip.di.impl

import com.tajmoti.commonutils.PageSourceLoader
import com.tajmoti.libopensubtitles.OpenSubtitlesFallbackService
import com.tajmoti.libprimewiretvprovider.PrimewireTvProvider
import com.tajmoti.libtulip.di.IBusinessLogicModule
import com.tajmoti.libtulip.model.hosted.StreamingService
import com.tajmoti.libtulip.model.subtitle.SubtitleInfo
import com.tajmoti.libtulip.repository.HostedTvDataRepository
import com.tajmoti.libtulip.repository.ItemMappingRepository
import com.tajmoti.libtulip.repository.TmdbTvDataRepository
import com.tajmoti.libtulip.service.MappingSearchService
import com.tajmoti.libtulip.service.StreamExtractionService
import com.tajmoti.libtulip.service.StreamService
import com.tajmoti.libtulip.service.SubtitleService
import com.tajmoti.libtulip.service.impl.MappingSearchServiceImpl
import com.tajmoti.libtulip.service.impl.StreamServiceImpl
import com.tajmoti.libtvprovider.MultiTvProvider
import com.tajmoti.libtvprovider.kinox.KinoxTvProvider
import com.tajmoti.libtvprovider.southpark.SouthParkTvProvider
import com.tajmoti.libtvvideoextractor.VideoLinkExtractor

object BusinessLogicModuleImpl : IBusinessLogicModule {

    override fun provideStreamService(
        hostedTvDataRepository: HostedTvDataRepository,
        extractionService: StreamExtractionService,
        hostedToTmdbMappingRepository: ItemMappingRepository
    ): StreamService {
        return StreamServiceImpl(
            hostedTvDataRepository,
            extractionService,
            hostedToTmdbMappingRepository
        )
    }

    override fun provideSubtitleService(
        openSubtitlesFallbackService: OpenSubtitlesFallbackService
    ): SubtitleService {
        return object : SubtitleService {
            override suspend fun downloadSubtitleToFile(info: SubtitleInfo, directory: String): Result<String> {
                return runCatching { TODO("provideSubtitleService") }
            }
        }
    }

    override fun provideMappingSearchService(
        hostedRepository: HostedTvDataRepository,
        tmdbRepository: TmdbTvDataRepository,
        hostedToTmdbMappingRepository: ItemMappingRepository,
    ): MappingSearchService {
        return MappingSearchServiceImpl(
            hostedRepository,
            tmdbRepository,
            hostedToTmdbMappingRepository,
        )
    }

    override fun provideMultiTvProvider(
        loader: PageSourceLoader,
        blacklist: Set<StreamingService>
    ): MultiTvProvider<StreamingService> {
        val primewire = PrimewireTvProvider(loader)
        val kinox = KinoxTvProvider(loader)
        val southPark = SouthParkTvProvider(loader)
        val impls = mapOf(
            "PRIMEWIRE" to primewire,
            "KINOX" to kinox,
            "SOUTH_PARK" to southPark
        )
        return MultiTvProvider(
            impls.filterKeys { !blacklist.contains(it) },
            30_000L
        )
    }

    override fun provideLinkExtractor(loader: PageSourceLoader): VideoLinkExtractor {
        return VideoLinkExtractor(loader)
    }
}