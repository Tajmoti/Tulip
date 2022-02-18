package ui.player

import com.tajmoti.libtulip.ui.streams.LoadedLink

sealed interface VideoPlayerStatus {
    object Loading : VideoPlayerStatus
    data class Loaded(val link: LoadedLink) : VideoPlayerStatus
    object Error : VideoPlayerStatus
}