package com.tajmoti.tulip.ui.library

import com.tajmoti.libtulip.repository.FavoritesRepository
import com.tajmoti.libtulip.repository.HostedTvDataRepository
import com.tajmoti.libtulip.repository.PlayingHistoryRepository
import com.tajmoti.libtulip.repository.TmdbTvDataRepository
import com.tajmoti.libtulip.ui.library.LibraryViewModel
import com.tajmoti.libtulip.ui.library.LibraryViewModelImpl
import com.tajmoti.tulip.ui.utils.DelegatingViewModel
import com.tajmoti.tulip.ui.utils.delegatingViewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AndroidLibraryViewModel @Inject constructor(
    favoritesRepo: FavoritesRepository,
    historyRepository: PlayingHistoryRepository,
    hostedRepo: HostedTvDataRepository,
    tmdbRepo: TmdbTvDataRepository,
) : DelegatingViewModel<LibraryViewModel>() {
    override val impl = LibraryViewModelImpl(
        favoritesRepo,
        historyRepository,
        hostedRepo,
        tmdbRepo,
        delegatingViewModelScope
    )
}