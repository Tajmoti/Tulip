package com.tajmoti.libtulip.ui.library

import com.tajmoti.commonutils.map
import com.tajmoti.libtulip.ui.StateViewModel
import kotlinx.coroutines.flow.StateFlow

interface LibraryViewModel : StateViewModel<LibraryViewModel.State> {
    /**
     * All items that the user has marked as favorite
     */
    val favoriteItems: StateFlow<List<LibraryItem>>
        get() = state.map(viewModelScope, State::favoriteItems)

    data class State(
        /**
         * All items that the user has marked as favorite
         */
        val favoriteItems: List<LibraryItem>
    )
}