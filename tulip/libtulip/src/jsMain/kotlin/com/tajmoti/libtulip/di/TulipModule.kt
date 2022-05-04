package com.tajmoti.libtulip.di

import com.tajmoti.libtulip.di.impl.ApiServiceModuleImpl
import com.tajmoti.libtulip.di.impl.BusinessLogicModuleImpl
import com.tajmoti.libtulip.di.impl.DataRepositoryModuleImpl
import com.tajmoti.libtulip.di.impl.NetworkingModuleImpl
import com.tajmoti.libtulip.facade.VideoDownloadFacade
import com.tajmoti.libtulip.misc.HardcodedConfigStore
import com.tajmoti.libtulip.misc.webdriver.KtorWebDriver
import com.tajmoti.libtulip.repository.*
import com.tajmoti.libtulip.facade.StubVideoDownloadFacade
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
    single { DataRepositoryModuleImpl.bindHostedTvDataRepository(get(), get(), get(), get(), get(), get()) }
    single { DataRepositoryModuleImpl.provideItemMappingRepository(get(), get()) }
    single { DataRepositoryModuleImpl.provideTmdbTvDataRepository(get(), get(), get(), get(), get()) }
    single { DataRepositoryModuleImpl.providePlayingHistoryRepository(get()) }
}

private val businessLogicModule = module {
    single { BusinessLogicModuleImpl.provideSubtitleService(get()) }
    single { BusinessLogicModuleImpl.provideMappingSearchService(get(), get(), get()) }
    single { BusinessLogicModuleImpl.provideMultiTvProvider(get(), setOf("PRIMEWIRE")) }
    single { BusinessLogicModuleImpl.provideLinkExtractor(get()) }
    single { BusinessLogicModuleImpl.provideTvShowInfoFacade(get(), get(), get()) }
    single { BusinessLogicModuleImpl.provideUserFavoriteFacade(get(), get(), get(), get()) }
    single { BusinessLogicModuleImpl.providePlayingProgressFacade(get()) }
    single { BusinessLogicModuleImpl.provideStreamFacade(get(), get(), get(), get()) }
    single { BusinessLogicModuleImpl.provideSubtitleFacade(get(), get()) }
    single<VideoDownloadFacade> { StubVideoDownloadFacade() }
}

private val dataSourceModule = module {
    single<HostedEpisodeRepository> { BrowserHostedEpisodeRepository() }
    single<HostedMovieRepository> { BrowserHostedMovieRepository() }
    single<HostedSeasonRepository> { BrowserHostedSeasonRepository(get()) }
    single<HostedTvShowRepository> { BrowserHostedTvShowRepository() }
    single<TmdbEpisodeRepository> { BrowserTmdbEpisodeRepository() }
    single<TvShowMappingRepository> { BrowserTvShowMappingRepository() }
    single<MovieMappingRepository> { BrowserMovieMappingRepository() }
    single<TmdbMovieRepository> { BrowserTmdbMovieRepository() }
    single<TmdbSeasonRepository> { BrowserTmdbSeasonRepository(get()) }
    single<TmdbTvShowRepository> { BrowserTmdbTvShowRepository() }
    single<UserFavoriteRepository> { BrowserUserFavoriteRepository() }
    single<UserPlayingProgressRepository> { BrowserUserPlayingProgressRepository() }
}

private val screenModule = module {
    factory<TvShowViewModel> { p -> TvShowViewModelImpl(get(), get(), get(), p.get(), p.get()) }
    factory<LibraryViewModel> { p -> LibraryViewModelImpl(get(), p.get()) }
    factory<SearchViewModel> { p -> SearchViewModelImpl(get(), p.get()) }
    factory<VideoPlayerViewModel> { p -> VideoPlayerViewModelImpl(get(), get(),get(), get(), get(), get(), get(), p.get(), p.get()) }
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