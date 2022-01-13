package com.tajmoti.tulip.ui.show

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.tajmoti.libtulip.repository.FavoritesRepository
import com.tajmoti.libtulip.repository.HostedTvDataRepository
import com.tajmoti.libtulip.repository.PlayingHistoryRepository
import com.tajmoti.libtulip.repository.TmdbTvDataRepository
import com.tajmoti.libtulip.ui.tvshow.TvShowViewModel
import com.tajmoti.libtulip.ui.tvshow.TvShowViewModelImpl
import com.tajmoti.tulip.ui.utils.DelegatingViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AndroidTvShowViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    hostedTvDataRepository: HostedTvDataRepository,
    tmdbRepo: TmdbTvDataRepository,
    favoritesRepository: FavoritesRepository,
    historyRepository: PlayingHistoryRepository
) : DelegatingViewModel<TvShowViewModel>() {
    override val impl = TvShowViewModelImpl(
        hostedTvDataRepository,
        tmdbRepo,
        favoritesRepository,
        historyRepository,
        viewModelScope,
        TvShowFragmentArgs.fromSavedStateHandle(savedStateHandle).itemKey
    )
}