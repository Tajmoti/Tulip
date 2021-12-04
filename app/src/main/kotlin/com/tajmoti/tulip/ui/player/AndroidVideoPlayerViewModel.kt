package com.tajmoti.tulip.ui.player

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.tajmoti.libtulip.repository.*
import com.tajmoti.libtulip.service.StreamExtractionService
import com.tajmoti.libtulip.service.StreamService
import com.tajmoti.libtulip.service.SubtitleService
import com.tajmoti.libtulip.service.VideoDownloadService
import com.tajmoti.libtulip.ui.player.VideoPlayerViewModel
import com.tajmoti.libtulip.ui.player.VideoPlayerViewModelImpl
import com.tajmoti.tulip.ui.DelegatingViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class AndroidVideoPlayerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    subtitleRepository: SubtitleRepository,
    playingHistoryRepository: PlayingHistoryRepository,
    tmdbTvDataRepository: TmdbTvDataRepository,
    hostedTvDataRepository: HostedTvDataRepository,
    downloadService: VideoDownloadService,
    extractionService: StreamExtractionService,
    streamService: StreamService,
    subtitleService: SubtitleService,
    @ApplicationContext
    context: Context
) : DelegatingViewModel<VideoPlayerViewModel>() {
    override val impl = run {
        val args = VideoPlayerActivityArgs.fromSavedStateHandle(savedStateHandle)
        VideoPlayerViewModelImpl(
            subtitleRepository,
            playingHistoryRepository,
            tmdbTvDataRepository,
            hostedTvDataRepository,
            downloadService,
            extractionService,
            streamService,
            subtitleService,
            context.getExternalFilesDir(null)!!,
            viewModelScope,
            args.streamableKey
        )
    }
}