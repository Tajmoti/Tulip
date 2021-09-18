package com.tajmoti.tulip.ui.library

import androidx.lifecycle.viewModelScope
import com.tajmoti.libtulip.data.HostedInfoDataSource
import com.tajmoti.libtulip.repository.FavoritesRepository
import com.tajmoti.libtulip.repository.TmdbTvDataRepository
import com.tajmoti.libtulip.ui.library.LibraryViewModel
import com.tajmoti.libtulip.ui.library.LibraryViewModelImpl
import com.tajmoti.tulip.ui.DelegatingViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AndroidLibraryViewModel @Inject constructor(
    favoritesRepo: FavoritesRepository,
    hostedRepo: HostedInfoDataSource,
    tmdbRepo: TmdbTvDataRepository
) : DelegatingViewModel<LibraryViewModel>() {
    override val impl = LibraryViewModelImpl(
        favoritesRepo,
        hostedRepo,
        tmdbRepo,
        viewModelScope
    )
}