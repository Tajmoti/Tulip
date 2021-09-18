package com.tajmoti.tulip.ui.player

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.tajmoti.libtulip.model.key.StreamableKey
import com.tajmoti.libtulip.repository.SubtitleRepository
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
    @ApplicationContext
    context: Context
) : DelegatingViewModel<VideoPlayerViewModel>() {
    override val impl = VideoPlayerViewModelImpl(
        subtitleRepository,
        context.getExternalFilesDir(null)!!,
        viewModelScope,
        VideoPlayerActivityArgs.fromSavedStateHandle(savedStateHandle).streamableKey as StreamableKey.Tmdb
    )
}