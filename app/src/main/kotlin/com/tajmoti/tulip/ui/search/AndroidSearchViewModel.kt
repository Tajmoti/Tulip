package com.tajmoti.tulip.ui.search

import com.tajmoti.libtulip.facade.SearchFacade
import com.tajmoti.libtulip.ui.search.SearchViewModel
import com.tajmoti.libtulip.ui.search.SearchViewModelImpl
import com.tajmoti.tulip.ui.utils.DelegatingViewModel
import com.tajmoti.tulip.ui.utils.delegatingViewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AndroidSearchViewModel @Inject constructor(
    mappingSearchService: SearchFacade
) : DelegatingViewModel<SearchViewModel>() {
    override val impl = SearchViewModelImpl(mappingSearchService, delegatingViewModelScope)
}