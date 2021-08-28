package com.tajmoti.libtulip.model.key

import com.tajmoti.libtulip.model.hosted.StreamingService
import com.tajmoti.libtulip.model.tmdb.TmdbItemId
import java.io.Serializable

interface ItemKey : Serializable {
    sealed interface Hosted : ItemKey {
        val streamingService: StreamingService
        val key: String
    }

    sealed interface Tmdb : ItemKey {
        val id: TmdbItemId
    }
}