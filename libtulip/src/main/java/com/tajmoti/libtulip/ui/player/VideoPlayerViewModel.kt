package com.tajmoti.libtulip.ui.player

import com.tajmoti.libtulip.model.info.TulipEpisodeInfo
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.StreamableKey
import com.tajmoti.libtulip.model.subtitle.SubtitleInfo
import kotlinx.coroutines.flow.StateFlow
import java.io.File

interface VideoPlayerViewModel {
    /**
     * Key of the media to play.
     */
    val streamableKey: StateFlow<StreamableKey>

    /**
     * True if the playing item is a TV show, false if it is a movie.
     */
    val isTvShow: StateFlow<Boolean>


    /**
     * All episodes from the playing season or null if a movie is being played.
     */
    val episodeList: StateFlow<List<TulipEpisodeInfo>?>

    /**
     * Next episode to play or null if this is the last one
     * or a movie is being played.
     */
    val nextEpisode: StateFlow<EpisodeKey?>


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
     * Positive values mean that subtitles must be delayed,
     * negative values mean they need to be shown earlier.
     */
    val subtitleOffset: StateFlow<Long>

    /**
     * If true show play, if false show pause, if null don't show at all
     */
    val showPlayButton: StateFlow<PlayButtonState>

    /**
     * True if the video is currently playing
     */
    val isPlaying: StateFlow<Boolean>

    /**
     * Buffering progress or null if not buffering
     */
    val buffering: StateFlow<Float?>

    /**
     * Playing position or null if position should be hidden
     */
    val position: StateFlow<Position?>

    /**
     * Whether the media has successfully played until the end
     */
    val isDonePlaying: StateFlow<Boolean>

    /**
     * True if an error was encountered and the player was canceled
     */
    val isError: StateFlow<Boolean>

    /**
     * Starts playback of the next episode or does nothing
     * if there's none or this is a movie.
     */
    fun goToNextEpisode()

    /**
     * A new media is attached and starting to be played.
     * If some media was already played, the position of the new
     * one will be set to the last valid position of the last media.
     */
    fun onMediaAttached(media: MediaPlayerHelper)

    /**
     * The existing media is detached.
     */
    fun onMediaDetached()

    /**
     * The user has selected which subtitles they wish to use.
     * The subtitles need to be downloaded before the video is played.
     */
    fun onSubtitlesSelected(subtitleInfo: SubtitleInfo?)

    /**
     * The user heard a word that they want to match to some text.
     */
    fun onWordHeard(time: Long)

    /**
     * The user saw text that they want to match to a heard word.
     */
    fun onTextSeen(time: Long)

    enum class PlayButtonState {
        SHOW_PLAY,
        SHOW_PAUSE,
        HIDE
    }
}