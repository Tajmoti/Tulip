package com.tajmoti.tulip.ui.player

import com.tajmoti.libtulip.model.subtitle.SubtitleInfo
import kotlinx.coroutines.flow.StateFlow
import java.io.File

interface VideoPlayerViewModel {
    /**
     * Successful result of subtitle loading.
     */
    val subtitleList: StateFlow<List<SubtitleInfo>>

    /**
     * Whether the list of available subtitles is being loaded right now.
     */
    val loadingSubtitles: StateFlow<Boolean>

    /**
     * Whether subtitles are loaded and can be selected.
     */
    val subtitlesReadyToSelect: StateFlow<Boolean>

    /**
     * Whether some subtitle file is being downloaded right now.
     */
    val downloadingSubtitleFile: StateFlow<Boolean>

    /**
     * Whether there was some error while downloading the subtitles.
     */
    val downloadingError: StateFlow<Boolean>

    /**
     * Subtitles to be applied to the currently playing video
     */
    val subtitleFile: StateFlow<File?>

    /**
     * How much the subtitles should be offset.
     * Positive values mean subtitles should be shown earlier.
     */
    val subtitleOffset: StateFlow<Long>

    /**
     * If true show play, if false show pause, if null don't show at all
     */
    val showPlayButton: StateFlow<PlayButtonState>

    /**
     * Buffering progress or null if not buffering
     */
    val buffering: StateFlow<Float?>

    /**
     * Playing position or null if not playing
     */
    val position: StateFlow<Position?>

    /**
     * True if an error was encountered and the player was canceled
     */
    val isError: StateFlow<Boolean>

    enum class PlayButtonState {
        SHOW_PLAY,
        SHOW_PAUSE,
        HIDE
    }
}