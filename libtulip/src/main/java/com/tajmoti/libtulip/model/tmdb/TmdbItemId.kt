package com.tajmoti.libtulip.model.tmdb

import java.io.Serializable

sealed class TmdbItemId : Serializable {
    abstract val id: Long

    data class Tv(override val id: Long) : TmdbItemId()
    data class Movie(override val id: Long) : TmdbItemId()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TmdbItemId) return false
        if (id != other.id) return false
        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
