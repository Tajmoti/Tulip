package com.tajmoti.tulip.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tajmoti.libtulip.model.info.TulipItemInfo
import com.tajmoti.libtulip.service.UserFavoritesService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val favoritesService: UserFavoritesService
) : ViewModel() {
    private val _state = MutableStateFlow<List<TulipItemInfo>>(emptyList())

    /**
     * All items that the user has marked as favorite
     */
    val favoriteItems: StateFlow<List<TulipItemInfo>> = _state

    init {
        fetchFavorites()
    }


    private fun fetchFavorites() {
        viewModelScope.launch {
            val flow = favoritesService.getUserFavoritesAsFlow()
            _state.emitAll(flow)
        }
    }
}