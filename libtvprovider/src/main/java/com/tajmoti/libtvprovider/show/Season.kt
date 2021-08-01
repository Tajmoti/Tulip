package com.tajmoti.libtvprovider.show

import com.tajmoti.libtvprovider.Marshallable
import com.tajmoti.libtvprovider.stream.VideoStreamRef
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.io.Serializable

interface Season: Marshallable {
    /**
     * One-based season number
     */
    val number: Int

    val episodes: List<Episode>


    suspend fun loadSourcesForAllEpisodes(): List<Pair<Episode, Result<List<VideoStreamRef>>>> {
        return coroutineScope {
            val coroutines = episodes.map { async { it to it.loadSources() } }
            awaitAll(*coroutines.toTypedArray())
        }
    }

    data class Info(
        /**
         * One-based season number
         */
        val number: Int,
        val episodeInfo: List<Episode.Info>
    ): Serializable
}