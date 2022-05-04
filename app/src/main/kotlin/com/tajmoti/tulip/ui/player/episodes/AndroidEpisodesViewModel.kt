package com.tajmoti.tulip.ui.player.episodes

import androidx.lifecycle.SavedStateHandle
import com.tajmoti.libtulip.facade.PlayingProgressFacade
import com.tajmoti.libtulip.facade.TvShowInfoFacade
import com.tajmoti.libtulip.facade.UserFavoriteFacade
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.ui.tvshow.TvShowViewModel
import com.tajmoti.libtulip.ui.tvshow.TvShowViewModelImpl
import com.tajmoti.tulip.ui.utils.DelegatingViewModel
import com.tajmoti.tulip.ui.utils.delegatingViewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AndroidEpisodesViewModel @Inject constructor(
    tvShowInfoFacade: TvShowInfoFacade,
    playingProgressFacade: PlayingProgressFacade,
    favoriteFacade: UserFavoriteFacade,
    savedStateHandle: SavedStateHandle,
) : DelegatingViewModel<TvShowViewModel>() {
    override val impl = TvShowViewModelImpl(
        tvShowInfoFacade,
        playingProgressFacade,
        favoriteFacade,
        delegatingViewModelScope,
        savedStateHandle.get<TvShowKey>(EpisodesFragment.ARG_TV_SHOW_KEY)!!
    )
}