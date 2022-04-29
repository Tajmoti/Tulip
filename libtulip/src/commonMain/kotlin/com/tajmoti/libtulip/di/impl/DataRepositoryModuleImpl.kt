package com.tajmoti.libtulip.di.impl

import com.tajmoti.libopensubtitles.OpenSubtitlesFallbackService
import com.tajmoti.libopensubtitles.OpenSubtitlesService
import com.tajmoti.libtulip.service.impl.LibTmdbRepository
import com.tajmoti.libtmdb.TmdbService
import com.tajmoti.libtulip.TulipConfiguration
import com.tajmoti.libtulip.data.HostedInfoDataSource
import com.tajmoti.libtulip.data.LocalTvDataSource
import com.tajmoti.libtulip.data.UserDataDataSource
import com.tajmoti.libtulip.di.IDataRepositoryModule
import com.tajmoti.libtulip.model.hosted.StreamingService
import com.tajmoti.libtulip.repository.*
import com.tajmoti.libtulip.repository.impl.*
import com.tajmoti.libtulip.service.StreamExtractionService
import com.tajmoti.libtulip.service.impl.StreamExtractionServiceImpl
import com.tajmoti.libtvprovider.MultiTvProvider
import com.tajmoti.libtvvideoextractor.VideoLinkExtractor
import io.ktor.client.*

object DataRepositoryModuleImpl : IDataRepositoryModule {

    override fun bindHostedTvDataRepository(
        hostedTvDataRepo: HostedInfoDataSource,
        tvProvider: MultiTvProvider<StreamingService>,
        tmdbRepo: TmdbTvDataRepository,
        config: TulipConfiguration
    ): HostedTvDataRepository {
        return HostedTvDataRepositoryImpl(hostedTvDataRepo, tvProvider, tmdbRepo, config)
    }

    override fun provideItemMappingRepository(
        hostedTvDataRepo: HostedInfoDataSource
    ): ItemMappingRepository {
        return ItemMappingRepositoryImpl(hostedTvDataRepo)
    }

    override fun provideStreamsRepository(
        linkExtractor: VideoLinkExtractor,
        httpClient: HttpClient
    ): StreamExtractionService {
        return StreamExtractionServiceImpl(linkExtractor, httpClient)
    }

    override fun provideTmdbTvDataRepository(
        service: TmdbService,
        db: LocalTvDataSource,
        config: TulipConfiguration
    ): TmdbTvDataRepository {
        return CachingTvDataRepository(LibTmdbRepository(service), db, config.tmdbCacheParams)
    }

    override fun provideFavoritesRepository(repo: UserDataDataSource): FavoritesRepository {
        return FavoriteRepositoryImpl(repo)
    }

    override fun provideSubtitleRepository(
        openSubtitlesService: OpenSubtitlesService,
        openSubtitlesFallbackService: OpenSubtitlesFallbackService,
    ): SubtitleRepository {
        return SubtitleRepositoryImpl(openSubtitlesService, openSubtitlesFallbackService)
    }

    override fun providePlayingHistoryRepository(dataSource: UserDataDataSource): PlayingHistoryRepository {
        return PlayingHistoryRepositoryImpl(dataSource)
    }
}