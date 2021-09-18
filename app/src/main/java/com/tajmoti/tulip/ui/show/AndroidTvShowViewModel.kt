package com.tajmoti.tulip.ui.show

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.tajmoti.libtulip.repository.FavoritesRepository
import com.tajmoti.libtulip.repository.HostedTvDataRepository
import com.tajmoti.libtulip.repository.TmdbTvDataRepository
import com.tajmoti.libtulip.ui.tvshow.TvShowViewModel
import com.tajmoti.libtulip.ui.tvshow.TvShowViewModelImpl
import com.tajmoti.tulip.ui.DelegatingViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * A ViewModel, which delegates its actual implementation.
 * To be used in conjunction with [viewModelsDelegated] like so:
 * <pre>
 * private val viewModel by viewModelsDelegated<TvShowViewModel, AndroidTvShowViewModel>()
 * </pre>
 */
@HiltViewModel
class AndroidTvShowViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    hostedTvDataRepository: HostedTvDataRepository,
    tmdbRepo: TmdbTvDataRepository,
    favoritesRepository: FavoritesRepository
) : DelegatingViewModel<TvShowViewModel>() {
    override val impl = TvShowViewModelImpl(
        hostedTvDataRepository,
        tmdbRepo,
        favoritesRepository,
        viewModelScope,
        TvShowFragmentArgs.fromSavedStateHandle(savedStateHandle).itemKey
    )
}