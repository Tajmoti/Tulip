package com.tajmoti.libtmdb.model.search

import com.tajmoti.libtmdb.model.FindResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TvSearchResult(
    @SerialName("id")
    override val id: Long,
    @SerialName("name")
    override val name: String,
    @SerialName("poster_path")
    override val posterPath: String?,
    @SerialName("backdrop_path")
    override val backdropPath: String?
) : FindResult
