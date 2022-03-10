package com.tajmoti.libtulip.di

import com.tajmoti.libtulip.data.*
import com.tajmoti.libtulip.di.impl.ApiServiceModuleImpl
import com.tajmoti.libtulip.di.impl.BusinessLogicModuleImpl
import com.tajmoti.libtulip.di.impl.DataRepositoryModuleImpl
import com.tajmoti.libtulip.di.impl.NetworkingModuleImpl
import com.tajmoti.libtulip.misc.HardcodedConfigStore
import com.tajmoti.libtulip.misc.webdriver.KtorWebDriver
import com.tajmoti.libtulip.service.VideoDownloadService
import com.tajmoti.libtulip.service.impl.StubVideoDownloadService
import com.tajmoti.libtulip.ui.library.LibraryViewModel
import com.tajmoti.libtulip.ui.library.LibraryViewModelImpl
import com.tajmoti.libtulip.ui.player.VideoPlayerViewModel
import com.tajmoti.libtulip.ui.player.VideoPlayerViewModelImpl
import com.tajmoti.libtulip.ui.search.SearchViewModel
import com.tajmoti.libtulip.ui.search.SearchViewModelImpl
import com.tajmoti.libtulip.ui.tvshow.TvShowViewModel
import com.tajmoti.libtulip.ui.tvshow.TvShowViewModelImpl
import com.tajmoti.libwebdriver.TulipWebDriver
import org.koin.core.qualifier.qualifier
import org.koin.dsl.module

private val configModule = module {
    single { HardcodedConfigStore.tulipConfiguration }
}

private val networkModule = module {
    single { NetworkingModuleImpl.makeHttpGetter(get(), get(qualifier(ProxyType.PROXY)), get(qualifier(ProxyType.DIRECT))) }
    single<TulipWebDriver> { KtorWebDriver(get()) }
}

private val apiServiceModule = module {
    single { ApiServiceModuleImpl.provideTmdbService(get(), get()) }
    single { ApiServiceModuleImpl.provideOpenSubtitlesService(get(), get()) }
    single { ApiServiceModuleImpl.provideOpenSubtitlesFallbackService(get(), get()) }
}

private val dataRepositoryModule = module {
    single { DataRepositoryModuleImpl.bindHostedTvDataRepository(get(), get(), get(), get()) }
    single { DataRepositoryModuleImpl.provideItemMappingRepository(get()) }
    single { DataRepositoryModuleImpl.provideStreamsRepository(get(), get()) }
    single { DataRepositoryModuleImpl.provideTmdbTvDataRepository(get(), get(), get()) }
    single { DataRepositoryModuleImpl.provideFavoritesRepository(get()) }
    single { DataRepositoryModuleImpl.provideSubtitleRepository(get(), get()) }
    single { DataRepositoryModuleImpl.providePlayingHistoryRepository(get()) }
}

private val businessLogicModule = module {
    single { BusinessLogicModuleImpl.provideStreamService(get(), get(), get()) }
    single { BusinessLogicModuleImpl.provideSubtitleService(get()) }
    single { BusinessLogicModuleImpl.provideMappingSearchService(get(), get(), get()) }
    single { BusinessLogicModuleImpl.provideMultiTvProvider(get(), setOf("PRIMEWIRE")) }
    single { BusinessLogicModuleImpl.provideLinkExtractor(get()) }
}

private val dataSourceModule = module {
    single<LocalTvDataSource> { BrowserTvDataSource() }
    single<HostedInfoDataSource> { BrowserHostedInfoDataSource() }
    single<UserDataDataSource> { BrowserUserInfoDataSource() }
    single<VideoDownloadService> { StubVideoDownloadService() }
}

private val screenModule = module {
    factory<TvShowViewModel> { p -> TvShowViewModelImpl(get(), get(), get(), get(), p.get(), p.get()) }
    factory<LibraryViewModel> { p -> LibraryViewModelImpl(get(), get(), get(), get(), p.get()) }
    factory<SearchViewModel> { p -> SearchViewModelImpl(get(), p.get()) }
    factory<VideoPlayerViewModel> { p -> VideoPlayerViewModelImpl(get(), get(),get(), get(),get(), get(),get(), get(), p.get(), p.get(), p.get()) }
}

val tulipModule = listOf(
    configModule,
    networkModule,
    apiServiceModule,
    dataRepositoryModule,
    businessLogicModule,
    dataSourceModule,
    screenModule
)