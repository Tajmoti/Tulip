package com.tajmoti.libtulip.facade

import com.tajmoti.commonutils.mapNotNulls
import com.tajmoti.libtulip.dto.EpisodePlayingProgressDto
import com.tajmoti.libtulip.dto.ItemPlayingProgressDto
import com.tajmoti.libtulip.dto.StreamablePlayingProgressDto
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.ItemKey
import com.tajmoti.libtulip.model.key.StreamableKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.repository.UserPlayingProgressRepository
import kotlinx.coroutines.flow.Flow

class PlayingProgressFacadeImpl(
    private val repository: UserPlayingProgressRepository
) : PlayingProgressFacade {

    override fun getPlayingProgress(key: ItemKey): Flow<ItemPlayingProgressDto?> {
        return repository.getLastPlayedPosition(key)
            .mapNotNulls { ItemPlayingProgressDto(it.key, it.progress) }
    }

    override fun getPlayingProgressForTvShow(key: TvShowKey): Flow<EpisodePlayingProgressDto?> {
        return repository.getLastPlayedPosition(key)
            .mapNotNulls { EpisodePlayingProgressDto(it.key as EpisodeKey, it.progress) }
    }

    override fun getPlayingProgressForStreamable(key: StreamableKey): Flow<StreamablePlayingProgressDto?> {
        return repository.getLastPlayedPosition(key)
            .mapNotNulls { StreamablePlayingProgressDto(it.progress) }
    }

    override suspend fun setPlayingProgress(key: StreamableKey, progress: Float) {
        repository.setLastPlayedPosition(key, progress)
    }

}