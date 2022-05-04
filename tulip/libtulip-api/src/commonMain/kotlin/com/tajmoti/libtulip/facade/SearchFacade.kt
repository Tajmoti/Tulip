package com.tajmoti.libtulip.facade

import com.tajmoti.libtulip.dto.SearchResultDto
import kotlinx.coroutines.flow.Flow

interface SearchFacade {

    fun search(query: String): Flow<Result<List<SearchResultDto>>>
}