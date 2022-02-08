package com.tajmoti.libtmdb.model.tv

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Episode(
    @SerialName("episode_number")
    val episodeNumber: Int,
    @SerialName("season_number")
    val seasonNumber: Int,
    @SerialName("name")
    val name: String,
    @SerialName("overview")
    val overview: String?,
    @SerialName("still_path")
    val stillPath: String?,
    @SerialName("vote_average")
    val voteAverage: Float?
)
