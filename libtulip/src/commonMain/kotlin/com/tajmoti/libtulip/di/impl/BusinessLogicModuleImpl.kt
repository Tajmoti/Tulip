package com.tajmoti.libtulip.di.impl

import com.tajmoti.libopensubtitles.OpenSubtitlesFallbackService
import com.tajmoti.libprimewiretvprovider.PrimewireTvProvider
import com.tajmoti.libtulip.HtmlGetter
import com.tajmoti.libtulip.di.IBusinessLogicModule
import com.tajmoti.libtulip.di.impl.NetworkingModuleImpl.makeWebViewGetter
import com.tajmoti.libtulip.di.impl.NetworkingModuleImpl.makeWebViewGetterWithCustomJs
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
import com.tajmoti.libwebdriver.TulipWebDriver

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
        webDriver: TulipWebDriver,
        htmlGetter: HtmlGetter,
    ): MultiTvProvider<StreamingService> {
        val webViewGetter = makeWebViewGetterWithCustomJs(webDriver)
        val primewire = PrimewireTvProvider(webViewGetter::load, htmlGetter::getHtml)
        val kinox = KinoxTvProvider(htmlGetter::getHtml)
        val southPark = SouthParkTvProvider(htmlGetter::getHtml)
        return MultiTvProvider(
            mapOf(
                StreamingService.PRIMEWIRE to primewire,
                StreamingService.KINOX to kinox,
                StreamingService.SOUTH_PARK to southPark
            ),
            30_000L
        )
    }

    override fun provideLinkExtractor(httpGetter: HtmlGetter, webDriver: TulipWebDriver): VideoLinkExtractor {
        val webViewGetter = makeWebViewGetter(webDriver)
        return VideoLinkExtractor(httpGetter::getHtml, webViewGetter)
    }
}