package com.tajmoti.tulip.ui.search

import androidx.lifecycle.viewModelScope
import com.tajmoti.libtulip.repository.HostedTvDataRepository
import com.tajmoti.libtulip.ui.search.SearchViewModel
import com.tajmoti.libtulip.ui.search.SearchViewModelImpl
import com.tajmoti.tulip.ui.DelegatingViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AndroidSearchViewModel @Inject constructor(
    repository: HostedTvDataRepository
) : DelegatingViewModel<SearchViewModel>() {
    override val impl = SearchViewModelImpl(repository, viewModelScope)
}