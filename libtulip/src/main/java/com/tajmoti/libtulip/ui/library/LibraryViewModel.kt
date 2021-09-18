package com.tajmoti.libtulip.ui.library

import kotlinx.coroutines.flow.StateFlow

interface LibraryViewModel {
    /**
     * All items that the user has marked as favorite
     */
    val favoriteItems: StateFlow<List<LibraryItem>>
}