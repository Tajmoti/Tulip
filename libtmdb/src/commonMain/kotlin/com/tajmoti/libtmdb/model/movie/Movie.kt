package com.tajmoti.libtmdb.model.movie

import com.tajmoti.libtmdb.model.FindResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Movie(
    @SerialName("id")
    override val id: Long,
    @SerialName("title")
    val title: String,
    @SerialName("overview")
    val overview: String?,
    @SerialName("poster_path")
    override val posterPath: String?,
    @SerialName("backdrop_path")
    override val backdropPath: String?
) : FindResult {
    override val name: String
        get() = title
}
