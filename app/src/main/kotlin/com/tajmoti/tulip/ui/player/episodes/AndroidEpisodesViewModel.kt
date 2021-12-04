package com.tajmoti.tulip.ui.player.episodes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.repository.FavoritesRepository
import com.tajmoti.libtulip.repository.HostedTvDataRepository
import com.tajmoti.libtulip.repository.TmdbTvDataRepository
import com.tajmoti.libtulip.ui.tvshow.TvShowViewModel
import com.tajmoti.libtulip.ui.tvshow.TvShowViewModelImpl
import com.tajmoti.tulip.ui.DelegatingViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AndroidEpisodesViewModel @Inject constructor(
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
        savedStateHandle.get<TvShowKey>(EpisodesFragment.ARG_TV_SHOW_KEY)!!
    )
}