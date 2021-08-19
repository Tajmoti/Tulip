package com.tajmoti.libtmdb.model

import com.squareup.moshi.Json

data class SearchTvResponse(
    @field:Json(name = "results")
    val results: List<TvResult>,
)