package com.tajmoti.libtulip.ui.library

import com.tajmoti.commonutils.map
import com.tajmoti.libtulip.facade.UserFavoriteFacade
import kotlinx.coroutines.CoroutineScope

class LibraryViewModelImpl constructor(
    userFavoriteFacade: UserFavoriteFacade,
    override val viewModelScope: CoroutineScope,
) : LibraryViewModel {

    private val favoriteItemsImpl = userFavoriteFacade.getUserFavorites()
        .stateInOffload(null)


    override val state = favoriteItemsImpl.map(viewModelScope) { LibraryViewModel.State(it) }
}