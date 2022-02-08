package com.tajmoti.libtmdb.model.tv

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Tv(
    @SerialName("id")
    val id: Long,
    @SerialName("name")
    val name: String,
    @SerialName("seasons")
    val seasons: List<SlimSeason>,
    @SerialName("poster_path")
    val posterPath: String?,
    @SerialName("backdrop_path")
    val backdropPath: String?
)
