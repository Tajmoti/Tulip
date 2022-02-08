package com.tajmoti.libtmdb.model.search

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchTvResponse(
    @SerialName("results")
    override val results: List<TvSearchResult>,
) : SearchResponse