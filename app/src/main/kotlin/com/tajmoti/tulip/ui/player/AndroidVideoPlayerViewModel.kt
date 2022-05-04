package com.tajmoti.tulip.ui.player

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.tajmoti.libtulip.facade.*
import com.tajmoti.libtulip.service.SubtitleService
import com.tajmoti.libtulip.ui.player.VideoPlayerViewModel
import com.tajmoti.libtulip.ui.player.VideoPlayerViewModelImpl
import com.tajmoti.tulip.ui.utils.DelegatingViewModel
import com.tajmoti.tulip.ui.utils.delegatingViewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class AndroidVideoPlayerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    tvShowInfoFacade: TvShowInfoFacade,
    streamService: StreamFacade,
    playingHistoryRepository: PlayingProgressFacade,
    downloadService: VideoDownloadFacade,
    subtitleRepository: SubtitleFacade,
    subtitleService: SubtitleService,
    @ApplicationContext
    context: Context
) : DelegatingViewModel<VideoPlayerViewModel>() {
    override val impl = run {
        val args = VideoPlayerActivityArgs.fromSavedStateHandle(savedStateHandle)
        VideoPlayerViewModelImpl(
            tvShowInfoFacade,
            streamService,
            playingHistoryRepository,
            downloadService,
            subtitleRepository,
            subtitleService,
            context.getExternalFilesDir(null)!!.absolutePath,
            delegatingViewModelScope,
            args.streamableKey
        )
    }
}