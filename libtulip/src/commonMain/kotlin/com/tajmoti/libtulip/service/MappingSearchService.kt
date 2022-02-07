package com.tajmoti.libtulip.service

import com.tajmoti.libtulip.model.hosted.MappedSearchResult
import com.tajmoti.libtulip.model.key.ItemKey
import com.tajmoti.libtulip.repository.ItemMappingRepository
import com.tajmoti.libtulip.repository.TmdbTvDataRepository
import kotlinx.coroutines.flow.Flow

interface MappingSearchService {

    /**
     * Searches [query] on all supported streaming services and returns a flow that emits the search results.
     *
     * Each result is attempted to be paired with an [ItemKey.Tmdb] which allows additional information about the item
     * to be retrieved from the [TmdbTvDataRepository].
     *
     * The mappings of [ItemKey.Hosted] to [ItemKey.Tmdb] for each search result are persisted
     * and can be later retrieved from [ItemMappingRepository].
     *
     * The flow will emit multiple values as more search results are loaded in.
     */
    fun searchAndCreateMappings(query: String): Flow<Result<List<MappedSearchResult>>>
}