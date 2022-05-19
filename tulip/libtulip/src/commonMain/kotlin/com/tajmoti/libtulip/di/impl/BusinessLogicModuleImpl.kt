package com.tajmoti.libtulip.di.impl

import com.tajmoti.commonutils.PageSourceLoader
import com.tajmoti.libopensubtitles.OpenSubtitlesFallbackService
import com.tajmoti.libopensubtitles.OpenSubtitlesService
import com.tajmoti.libprimewiretvprovider.PrimewireTvProvider
import com.tajmoti.libtulip.di.IBusinessLogicModule
import com.tajmoti.libtulip.facade.*
import com.tajmoti.libtulip.model.key.StreamingService
import com.tajmoti.libtulip.model.key.SubtitleKey
import com.tajmoti.libtulip.repository.UserFavoriteRepository
import com.tajmoti.libtulip.repository.UserPlayingProgressRepository
import com.tajmoti.libtulip.repository.impl.SubtitleFacadeImpl
import com.tajmoti.libtulip.service.*
import com.tajmoti.libtvprovider.MultiTvProvider
import com.tajmoti.libtvprovider.kinox.KinoxTvProvider
import com.tajmoti.libtvprovider.southpark.SouthParkTvProvider
import com.tajmoti.libtvvideoextractor.VideoLinkExtractor
import io.ktor.client.*

object BusinessLogicModuleImpl : IBusinessLogicModule {

    override fun provideSubtitleService(
        openSubtitlesFallbackService: OpenSubtitlesFallbackService
    ): SubtitleService {
        return object : SubtitleService {
            override suspend fun downloadSubtitleToFile(key: SubtitleKey, directory: String): Result<String> {
                return runCatching { TODO("provideSubtitleService") }
            }
        }
    }

    override fun provideSubtitleFacade(
        openSubtitlesService: OpenSubtitlesService,
        openSubtitlesFallbackService: OpenSubtitlesFallbackService,
    ): SubtitleFacade {
        return SubtitleFacadeImpl(openSubtitlesService, openSubtitlesFallbackService)
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

    override fun provideMappingSearchService(
        tvProvider: MultiTvProvider<StreamingService>,
        tmdbRepository: TmdbTvDataRepository,
        itemMappingRepository: ItemMappingRepository
    ): SearchFacade {
        return SearchFacadeImpl(tvProvider, tmdbRepository, itemMappingRepository)
    }

    override fun provideUserFavoriteFacade(
        favoritesRepository: UserFavoriteRepository,
        tmdbRepo: TmdbTvDataRepository,
        hostedTvDataRepository: HostedTvDataRepository,
        historyRepository: PlayingHistoryRepository
    ): UserFavoriteFacade {
        return UserFavoriteFacadeImpl(favoritesRepository, tmdbRepo, hostedTvDataRepository, historyRepository)
    }

    override fun provideTvShowInfoFacade(
        hostedTvDataRepository: HostedTvDataRepository,
        tmdbRepo: TmdbTvDataRepository,
        favoritesRepository: UserFavoriteRepository
    ): TvShowInfoFacade {
        return TvShowInfoFacadeImpl(hostedTvDataRepository, tmdbRepo, favoritesRepository)
    }

    override fun providePlayingProgressFacade(repository: UserPlayingProgressRepository): PlayingProgressFacade {
        return PlayingProgressFacadeImpl(repository)
    }

    override fun provideStreamFacade(
        hostedTvDataRepository: HostedTvDataRepository,
        hostedToTmdbMappingRepository: ItemMappingRepository,
        linkExtractor: VideoLinkExtractor,
        httpClient: HttpClient
    ): StreamFacade {
        return StreamFacadeImpl(hostedTvDataRepository, hostedToTmdbMappingRepository, linkExtractor, httpClient)
    }
}