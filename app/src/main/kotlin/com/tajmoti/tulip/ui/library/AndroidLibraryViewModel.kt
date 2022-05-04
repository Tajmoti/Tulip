package com.tajmoti.tulip.ui.library

import com.tajmoti.libtulip.facade.UserFavoriteFacade
import com.tajmoti.libtulip.ui.library.LibraryViewModel
import com.tajmoti.libtulip.ui.library.LibraryViewModelImpl
import com.tajmoti.tulip.ui.utils.DelegatingViewModel
import com.tajmoti.tulip.ui.utils.delegatingViewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AndroidLibraryViewModel @Inject constructor(
    userFavoriteFacade: UserFavoriteFacade,
) : DelegatingViewModel<LibraryViewModel>() {
    override val impl = LibraryViewModelImpl(
        userFavoriteFacade,
        delegatingViewModelScope
    )
}