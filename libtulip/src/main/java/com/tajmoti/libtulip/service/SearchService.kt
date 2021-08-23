package com.tajmoti.libtulip.service

import com.tajmoti.libtulip.model.info.TulipSearchResult

interface SearchService {

    suspend fun search(query: String): Result<List<TulipSearchResult>>
}