package com.tajmoti.libtulip.facade

import com.tajmoti.libtulip.dto.EpisodePlayingProgressDto
import com.tajmoti.libtulip.dto.ItemPlayingProgressDto
import com.tajmoti.libtulip.dto.StreamablePlayingProgressDto
import com.tajmoti.libtulip.model.key.ItemKey
import com.tajmoti.libtulip.model.key.StreamableKey
import com.tajmoti.libtulip.model.key.TvShowKey
import kotlinx.coroutines.flow.Flow

interface PlayingProgressFacade {

    fun getPlayingProgress(key: ItemKey): Flow<ItemPlayingProgressDto?>

    fun getPlayingProgressForTvShow(key: TvShowKey): Flow<EpisodePlayingProgressDto?>

    fun getPlayingProgressForStreamable(key: StreamableKey): Flow<StreamablePlayingProgressDto?>

    suspend fun setPlayingProgress(key: StreamableKey, progress: Float)
}