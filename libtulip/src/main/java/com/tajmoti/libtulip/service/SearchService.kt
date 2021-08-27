package com.tajmoti.libtulip.service

import com.tajmoti.libtulip.model.info.TulipSearchResult

/**
 * Searches on specific streaming sites and combines the results with TMDB data.
 */
interface SearchService {

    suspend fun search(query: String): Result<List<TulipSearchResult>>
}