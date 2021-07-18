package com.tajmoti.tulip.ui.search

import androidx.lifecycle.*
import com.tajmoti.libtvprovider.TvItem
import com.tajmoti.libtvprovider.TvProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val tvProvider: TvProvider
) : ViewModel() {
    private val _state = MutableLiveData<State>(State.Idle)
    val state: LiveData<State> = _state
    val idle = Transformations.map(state) { it is State.Idle }
    val loading = Transformations.map(state) { it is State.Searching }
    val noResults = Transformations.map(state) { it is State.Success && it.items.isEmpty() }


    /**
     * Search for a TV show or a movie.
     */
    fun search(query: String) {
        if (_state.value is State.Searching)
            return
        _state.value = State.Searching
        viewModelScope.launch { startFetchEpisodesAsync(query) }
    }

    private suspend fun startFetchEpisodesAsync(query: String) {
        try {
            val result = tvProvider.search(query)
            result.onSuccess { _state.value = State.Success(it) }
            result.onFailure { _state.value = State.Error(it.message ?: it.javaClass.name) }
        } catch (e: CancellationException) {
            _state.value = State.Idle
        }
    }

    sealed class State {
        object Idle : State()
        object Searching : State()
        data class Success(val items: List<TvItem>) : State()
        data class Error(val message: String) : State()

        val success: Boolean
            get() = this is Success
    }
}