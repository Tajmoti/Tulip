package com.tajmoti.libtmdb.model.tv

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Season(
    @SerialName("name")
    val name: String,
    @SerialName("overview")
    val overview: String?,
    @SerialName("season_number")
    val seasonNumber: Int,
    @SerialName("episodes")
    val episodes: List<Episode>
)
