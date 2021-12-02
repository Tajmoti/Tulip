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
import com.tajmoti.libtulip.repository.impl.*
import com.tajmoti.libtulip.service.StreamExtractionService
import com.tajmoti.libtulip.service.impl.StreamExtractionServiceImpl
import com.tajmoti.libtvprovider.MultiTvProvider
import com.tajmoti.libtvvideoextractor.VideoLinkExtractor
import dagger.Module
import dagger.Provides
import io.ktor.client.*
import javax.inject.Singleton

@Module
object DataRepositoryModule {
    @Provides
    @Singleton
    fun bindHostedTvDataRepository(
        hostedTvDataRepo: HostedInfoDataSource,
        tvProvider: MultiTvProvider<StreamingService>,
        tmdbRepo: TmdbTvDataRepository,
        config: TulipConfiguration
    ): HostedTvDataRepository {
        return HostedTvDataRepositoryImpl(hostedTvDataRepo, tvProvider, tmdbRepo, config)
    }

    @Provides
    @Singleton
    fun provideItemMappingRepository(
        hostedTvDataRepo: HostedInfoDataSource
    ): ItemMappingRepository {
        return ItemMappingRepositoryImpl(hostedTvDataRepo)
    }

    @Provides
    @Singleton
    fun provideStreamsRepository(
        linkExtractor: VideoLinkExtractor,
        httpClient: HttpClient
    ): StreamExtractionService {
        return StreamExtractionServiceImpl(linkExtractor, httpClient)
    }

    @Provides
    @Singleton
    fun provideTmdbTvDataRepository(
        service: TmdbService,
        db: LocalTvDataSource,
        config: TulipConfiguration
    ): TmdbTvDataRepository {
        return TmdbTvDataRepositoryImpl(service, db, config.tmdbCacheParams)
    }

    @Provides
    @Singleton
    fun provideFavoritesRepository(repo: UserDataDataSource): FavoritesRepository {
        return FavoriteRepositoryImpl(repo)
    }

    @Provides
    @Singleton
    fun provideSubtitleRepository(
        openSubtitlesService: OpenSubtitlesService,
        openSubtitlesFallbackService: OpenSubtitlesFallbackService,
    ): SubtitleRepository {
        return SubtitleRepositoryImpl(openSubtitlesService, openSubtitlesFallbackService)
    }

    @Provides
    @Singleton
    fun providePlayingHistoryRepository(dataSource: UserDataDataSource): PlayingHistoryRepository {
        return PlayingHistoryRepositoryImpl(dataSource)
    }
}