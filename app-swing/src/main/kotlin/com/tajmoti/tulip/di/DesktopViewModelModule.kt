package com.tajmoti.tulip.di

import com.tajmoti.libtulip.ui.search.SearchViewModel
import com.tajmoti.libtulip.ui.search.SearchViewModelImpl
import dagger.Binds
import dagger.Module

@Module
interface DesktopViewModelModule {

    @Binds
    fun bindSearchViewModel(vm: SearchViewModelImpl): SearchViewModel
}