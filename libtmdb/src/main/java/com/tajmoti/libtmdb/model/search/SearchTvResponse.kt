package com.tajmoti.libtmdb.model.search

import com.squareup.moshi.Json
import com.tajmoti.libtmdb.model.tv.Tv

data class SearchTvResponse(
    @field:Json(name = "results")
    val results: List<Tv>,
)