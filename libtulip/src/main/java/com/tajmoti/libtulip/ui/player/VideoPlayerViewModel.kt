package com.tajmoti.libtulip.ui.player

import com.tajmoti.libtulip.model.info.StreamableInfo
import com.tajmoti.libtulip.model.key.StreamableKey
import com.tajmoti.libtulip.model.stream.StreamableInfoWithLangLinks
import com.tajmoti.libtulip.model.stream.UnloadedVideoStreamRef
import com.tajmoti.libtulip.model.subtitle.SubtitleInfo
import com.tajmoti.libtulip.ui.streams.FailedLink
import com.tajmoti.libtulip.ui.streams.LoadedLink
import com.tajmoti.libtulip.ui.streams.SelectedLink
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

interface VideoPlayerViewModel {
    /**
     * Key of the streamable that is currently being loaded or played.
     */
    val streamableKey: StateFlow<StreamableKey>

    /**
     * Cancels playback of the previous streamable
     * and starts playing the one by [key].
     *
     * If the key is already being played, this method has no effect.
     */
    fun changeStreamable(key: StreamableKey)


    /**
     * True if the currently playing streamable is a TV show, false if it is a movie.
     */
    val isTvShow: StateFlow<Boolean>

    /**
     * Whether a TV show episode is currently playing and it's not the last one in the season.
     */
    val hasNextEpisode: StateFlow<Boolean>

    /**
     * Starts playback of the next episode or does nothing
     * if there's none or this is a movie.
     */
    fun goToNextEpisode()


    /**
     * Whether stream links are being loaded right now and there are no loaded links yet.
     */
    val linksLoading: StateFlow<Boolean>

    /**
     * Streamable info of [streamableKey] or null if not yet available.
     */
    val streamableInfo: StateFlow<StreamableInfo?>

    /**
     * Loaded links of the currently selected [streamableKey] or null if not yet available.
     * This value is updated in real time as more links are loaded in.
     */
    val linksResult: StateFlow<StreamableInfoWithLangLinks?>

    /**
     * Whether link list loading is finished and at least one stream was found.
     */
    val linksAnyResult: StateFlow<Boolean>

    /**
     * Whether link list loading is finished, but no streams were found.
     */
    val linksNoResult: StateFlow<Boolean>


    /**
     * The user has clicked a link, it needs to be resolved and played.
     */
    fun onStreamClicked(stream: UnloadedVideoStreamRef, download: Boolean)

    /**
     * Whether redirects are being resolved or a direct link is being loaded right now.
     */
    val loadingStreamOrDirectLink: StateFlow<Boolean>

    /**
     * Selected item which has redirects resolved, but doesn't support direct link loading.
     */
    val directLoadingUnsupported: SharedFlow<SelectedLink>

    /**
     * Selected item with direct link loaded.
     */
    val videoLinkToPlay: StateFlow<LoadedLink?>

    /**
     * Selected item for playing is loaded
     */
    val videoLinkToDownload: StateFlow<LoadedLink?>

    /**
     * Selected item failed to load redirects or failed to load direct link.
     */
    val linkLoadingError: SharedFlow<FailedLink>


    /**
     * Successful result of subtitle loading.
     */
    val subtitleList: StateFlow<List<SubtitleInfo>>

    /**
     * Whether the list of available subtitles is being loaded right now.
     */
    val loadingSubtitleList: StateFlow<Boolean>

    /**
     * Whether subtitles are loaded and can be selected.
     */
    val subtitlesReadyToSelect: StateFlow<Boolean>

    /**
     * Whether some subtitle file is being downloaded right now.
     */
    val downloadingSubtitles: StateFlow<Boolean>

    /**
     * Whether there was some error while downloading the selected subtitle file.
     */
    val subtitleDownloadError: StateFlow<Boolean>

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
     * State of the playback.
     */
    val mediaPlayerState: StateFlow<MediaPlayerState>

    /**
     * Whether the media has successfully played until the end
     */
    val isDonePlaying: StateFlow<Boolean>

    /**
     * True if an error was encountered and the player was canceled
     */
    val isError: StateFlow<Boolean>

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
    fun onWordHeard()

    /**
     * The user saw text that they want to match to a heard word.
     */
    fun onTextSeen()

    /**
     * Video needs to be skipped a few seconds forwards.
     */
    fun skipForwards()

    /**
     * Video needs to be skipped a few seconds backwards.
     */
    fun skipBackwards()

    /**
     * Video needs to be pause or resumed.
     */
    fun playPause()

    /**
     * Video needs to be sought to the provided progress (fraction).
     */
    fun setPlaybackProgress(progress: Float)

    enum class PlayButtonState {
        SHOW_PLAY,
        SHOW_PAUSE,
        HIDE
    }
}