package com.tajmoti.libtmdb.model.search

import com.tajmoti.libtmdb.model.FindResult

interface SearchResponse {
    val results: List<FindResult>
}