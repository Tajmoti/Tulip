package com.tajmoti.libtulip.di

import com.tajmoti.libtulip.data.HostedInfoDataSource
import com.tajmoti.libtulip.data.LocalTvDataSource
import com.tajmoti.libtulip.data.impl.InMemoryHostedInfoDataSource
import com.tajmoti.libtulip.data.impl.InMemoryLocalTvDataSource
import com.tajmoti.libtulip.di.impl.ApiServiceModuleImpl
import com.tajmoti.libtulip.di.impl.BusinessLogicModuleImpl
import com.tajmoti.libtulip.di.impl.DataRepositoryModuleImpl
import com.tajmoti.multiplatform.getAppHttpClient
import org.koin.dsl.module

val tulipModule = module {
    single { ApiServiceModuleImpl.provideTmdbService(get(), get()) }
    single { ApiServiceModuleImpl.provideOpenSubtitlesService(get(), get()) }
    single { ApiServiceModuleImpl.provideOpenSubtitlesFallbackService(get(), get()) }

    single { BusinessLogicModuleImpl.provideStreamService(get(), get(), get()) }
    single { BusinessLogicModuleImpl.provideSubtitleService(get()) }
    single { BusinessLogicModuleImpl.provideMappingSearchService(get(), get(), get()) }
    single { BusinessLogicModuleImpl.provideMultiTvProvider(get(), get()) }
    single { BusinessLogicModuleImpl.provideLinkExtractor(get(), get()) }
    single { BusinessLogicModuleImpl.makeWebViewGetterWithCustomJs(get()) }
    single { BusinessLogicModuleImpl.makeWebViewGetter(get()) }
    single { BusinessLogicModuleImpl.makeHttpGetter(get()) }

    single { DataRepositoryModuleImpl.bindHostedTvDataRepository(get(), get(), get(), get()) }
    single { DataRepositoryModuleImpl.provideItemMappingRepository(get()) }
    single { DataRepositoryModuleImpl.provideStreamsRepository(get(), get()) }
    single { DataRepositoryModuleImpl.provideTmdbTvDataRepository(get(), get(), get()) }
    single { DataRepositoryModuleImpl.provideFavoritesRepository(get()) }
    single { DataRepositoryModuleImpl.provideSubtitleRepository(get(), get()) }
    single { DataRepositoryModuleImpl.providePlayingHistoryRepository(get()) }

    single<LocalTvDataSource> { InMemoryLocalTvDataSource() }
    single<HostedInfoDataSource> { InMemoryHostedInfoDataSource() }
    single { getAppHttpClient() }
}