package com.tajmoti.libtulip.ui.library

import com.tajmoti.commonutils.map
import com.tajmoti.libtulip.ui.StateViewModel
import kotlinx.coroutines.flow.StateFlow

interface LibraryViewModel : StateViewModel<LibraryViewModel.State> {
    /**
     * All items that the user has marked as favorite.
     * If the items are loading, an empty list.
     */
    val favoriteItems: StateFlow<List<LibraryItem>>
        get() = state.map(viewModelScope) { it.favoriteItems ?: emptyList() }

    data class State(
        /**
         * All items that the user has marked as favorite.
         * Null if the items are loading.
         */
        val favoriteItems: List<LibraryItem>?
    )
}