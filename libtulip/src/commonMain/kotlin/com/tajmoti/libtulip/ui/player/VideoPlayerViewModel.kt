package com.tajmoti.libtulip.ui.player

import com.tajmoti.commonutils.map
import com.tajmoti.libtulip.model.info.LanguageCode
import com.tajmoti.libtulip.model.info.StreamableInfo
import com.tajmoti.libtulip.model.key.StreamableKey
import com.tajmoti.libtulip.model.stream.UnloadedVideoStreamRef
import com.tajmoti.libtulip.model.subtitle.SubtitleInfo
import com.tajmoti.libtulip.ui.streams.FailedLink
import com.tajmoti.libtulip.ui.streams.LoadedLink
import com.tajmoti.libtulip.ui.streams.SelectedLink
import com.tajmoti.libtvprovider.model.VideoStreamRef
import com.tajmoti.libtvvideoextractor.CaptchaInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*

interface VideoPlayerViewModel {
    val viewModelScope: CoroutineScope

    /**
     * State of this ViewModel.
     */
    val state: StateFlow<State>

    data class State(
        /**
         * Key of the streamable that is currently being loaded or played.
         */
        val streamableKey: StreamableKey? = null,
        /**
         * Streamable info of [State.streamableKey] or null if not yet available.
         */
        val streamableInfo: StreamableInfo? = null,
        /**
         * Status of video link list loading.
         */
        val linkListLoadingState: LinkListLoadingState = LinkListLoadingState.Loading,
        /**
         * Status of loading of a selected link.
         */
        val linkLoadingState: LinkLoadingState = LinkLoadingState.Idle,
        /**
         * Status of subtitle list loading.
         */
        val loadingSubtitlesState: SubtitleListLoadingState = SubtitleListLoadingState.Loading,
        /**
         * Status of selected subtitle file downloading.
         */
        val subtitleDownloadState: SubtitleDownloadingState = SubtitleDownloadingState.Idle,
        /**
         * Subtitle synchronization state.
         */
        val subSyncState: SubtitleSyncState? = null,
        /**
         * Whether a TV show episode is currently playing, and it's not the last one in the season.
         */
        val hasNextEpisode: Boolean = false
    )

    /**
     * Cancels playback of the previous streamable
     * and starts playing the one by [key].
     *
     * If the [key] is already being loaded played, this method has no effect.
     */
    fun changeStreamable(key: StreamableKey)

    /**
     * Starts playback of the next episode or does nothing
     * if there's none or this is a movie.
     */
    fun goToNextEpisode()


    /**
     * The user has clicked a link, it needs to be resolved and played.
     */
    fun onStreamClicked(stream: UnloadedVideoStreamRef, download: Boolean)


    /**
     * A new media is attached and starting to be played.
     * If some media was already played, the position of the new
     * one will be set to the last valid position of the last media.
     */
    fun onMediaAttached(media: VideoPlayer)

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
     * Video needs to be paused or resumed.
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

    sealed interface SubtitleListLoadingState {
        object Loading : SubtitleListLoadingState
        data class Success(val subtitles: List<SubtitleInfo>) : SubtitleListLoadingState
        object Error : SubtitleListLoadingState
    }

    sealed interface SubtitleDownloadingState {
        object Idle : SubtitleDownloadingState
        object Loading : SubtitleDownloadingState
        data class Success(val subtitles: String) : SubtitleDownloadingState
        object Error : SubtitleDownloadingState
    }

    sealed interface SubtitleSyncState {
        /**
         * Subtitle offset to use.
         * Positive values mean that subtitles must be delayed,
         * negative values mean they need to be shown earlier.
         */
        val offsetMs: Long

        class Heard(val time: Long, override val offsetMs: Long) : SubtitleSyncState
        class Seen(val time: Long, override val offsetMs: Long) : SubtitleSyncState
        class OffsetUsed(override val offsetMs: Long) : SubtitleSyncState
    }

    sealed interface LinkListLoadingState {
        /**
         * Marker state before the loading is started.
         */
        object Loading : LinkListLoadingState

        /**
         * Streamable item loaded successfully.
         */
        data class Success(
            val streams: List<UnloadedVideoStreamRef>,
            /**
             * Whether this is the final value and no more will be loaded.
             */
            val final: Boolean,
        ) : LinkListLoadingState

        /**
         * Error during loading of the item.
         */
        object Error : LinkListLoadingState
    }

    sealed interface LinkLoadingState {
        /**
         * The video stream that was selected.
         */
        val stream: VideoStreamRef?

        /**
         * Whether the stream is to be downloaded (else played).
         */
        val download: Boolean

        object Idle : LinkLoadingState {
            override val stream: VideoStreamRef? = null
            override val download = false
        }

        /**
         * The streaming page URL is being resolved.
         */
        data class Loading(
            override val stream: VideoStreamRef.Unresolved,
            override val download: Boolean,
        ) : LinkLoadingState

        /**
         * Direct link extraction is not supported for the clicked streaming site.
         */
        data class DirectLinkUnsupported(
            override val stream: VideoStreamRef.Resolved,
            override val download: Boolean,
        ) : LinkLoadingState

        /**
         * A direct video link is being extracted.
         */
        data class LoadingDirect(
            override val stream: VideoStreamRef.Resolved,
            override val download: Boolean,
        ) : LinkLoadingState

        /**
         * A direct video link was extracted successfully.
         */
        data class LoadedDirect(
            override val stream: VideoStreamRef.Resolved,
            override val download: Boolean,
            val directLink: String,
        ) : LinkLoadingState

        data class Error(
            override val stream: VideoStreamRef,
            val languageCode: LanguageCode,
            override val download: Boolean,
            val captcha: CaptchaInfo?,
        ) : LinkLoadingState
    }


    /**
     * True if the currently playing streamable is a TV show, false if it is a movie.
     */
    val isTvShow: StateFlow<Boolean>
        get() = state.map(viewModelScope, State::isTvShow)

    /**
     * Whether stream links are being loaded right now and there are no loaded links yet.
     */
    val linksLoading: StateFlow<Boolean>
        get() = state.map(viewModelScope, State::linksLoading)

    /**
     * Key of the streamable that is currently being loaded or played.
     */
    val streamableKey: StateFlow<StreamableKey?>
        get() = state.map(viewModelScope, State::streamableKey)

    /**
     * Streamable info of [State.streamableKey] or null if not yet available.
     */
    val streamableInfo: StateFlow<StreamableInfo?>
        get() = state.map(viewModelScope, State::streamableInfo)

    /**
     * Loaded links of the currently selected [State.streamableKey] or null if not yet available.
     * This value is updated in real time as more links are loaded in.
     */
    val linksResult: StateFlow<List<UnloadedVideoStreamRef>?>
        get() = state.map(viewModelScope, State::linksResult)

    /**
     * Whether link list loading is finished and at least one stream was found.
     */
    val linksAnyResult: StateFlow<Boolean>
        get() = state.map(viewModelScope, State::linksAnyResult)

    /**
     * Whether link list loading is finished, but no streams were found.
     */
    val linksNoResult: StateFlow<Boolean>
        get() = state.map(viewModelScope, State::linksNoResult)


    /**
     * Whether a direct link is being loaded right now.
     */
    val loadingStreamOrDirectLink: StateFlow<Boolean>
        get() = state.map(viewModelScope, State::loadingStreamOrDirectLink)

    /**
     * Selected item which has redirects resolved, but doesn't support direct link loading.
     */
    val directLoadingUnsupported: SharedFlow<SelectedLink>
        get() = state.mapNotNull { it.directLoadingUnsupported }
            .shareIn(viewModelScope, SharingStarted.Eagerly)

    /**
     * Link that was selected for playback. It might be loading, be already loaded, or errored out.
     */
    val videoLinkPreparingOrPlaying: StateFlow<VideoStreamRef?>
        get() = state.map(viewModelScope, State::videoLinkPreparingOrPlaying)

    /**
     * Selected item with direct link loaded.
     */
    val videoLinkToPlay: StateFlow<LoadedLink?>
        get() = state.map(viewModelScope, State::videoLinkToPlay)

    /**
     * Selected item for playing is loaded
     */
    val videoLinkToDownload: StateFlow<LoadedLink?>
        get() = state.map(viewModelScope, State::videoLinkToDownload)

    /**
     * Selected item failed to load redirects or failed to load direct link.
     */
    val linkLoadingError: SharedFlow<FailedLink>
        get() = state.mapNotNull { it.linkLoadingError }
            .shareIn(viewModelScope, SharingStarted.Eagerly)


    /**
     * Successful result of subtitle loading.
     */
    val subtitleList: StateFlow<List<SubtitleInfo>>
        get() = state.map(viewModelScope, State::subtitleList)

    /**
     * Whether the list of available subtitles is being loaded right now.
     */
    val loadingSubtitleList: StateFlow<Boolean>
        get() = state.map(viewModelScope, State::loadingSubtitleList)

    /**
     * Whether subtitles are loaded and can be selected.
     */
    val subtitlesReadyToSelect: StateFlow<Boolean>
        get() = state.map(viewModelScope, State::subtitlesReadyToSelect)

    /**
     * Whether some subtitles file is being downloaded right now.
     */
    val downloadingSubtitles: StateFlow<Boolean>
        get() = state.map(viewModelScope, State::downloadingSubtitles)

    /**
     * Whether there was some error while downloading the selected subtitle file.
     */
    val subtitleDownloadError: StateFlow<Boolean>
        get() = state.map(viewModelScope, State::subtitleDownloadError)

    /**
     * Subtitles to be applied to the currently playing video
     */
    val subtitleFile: StateFlow<String?>
        get() = state.map(viewModelScope, State::subtitleFile)

    /**
     * How much the subtitles should be offset.
     * Positive values mean that subtitles must be delayed,
     * negative values mean they need to be shown earlier.
     */
    val subtitleOffset: StateFlow<Long>
        get() = state.map(viewModelScope, State::subtitleOffset)

    /**
     * If true show play, if false show pause, if null don't show at all
     */
    val showPlayButton: StateFlow<PlayButtonState>
        get() = mediaPlayerState
            .map(viewModelScope, ::playerStateToPlayButtonState)


    /**
     * True if the video is currently playing
     */
    val isPlaying: StateFlow<Boolean>
        get() = mediaPlayerState
            .map(viewModelScope) { it is MediaPlayerState.Playing }

    /**
     * Buffering progress or null if not buffering
     */
    val buffering: StateFlow<Float?>
        get() = mediaPlayerState
            .map(viewModelScope) { state -> (state as? MediaPlayerState.Buffering)?.percent }

    /**
     * Playing position or null if position should be hidden
     */
    val position: StateFlow<Position?>
        get() = mediaPlayerState
            .map(viewModelScope) { state -> state.validPositionOrNull }

    /**
     * State of the playback.
     */
    val mediaPlayerState: StateFlow<MediaPlayerState>

    /**
     * Whether the media has successfully played until the end
     */
    val isDonePlaying: StateFlow<Boolean>
        get() = mediaPlayerState
            .map(viewModelScope) { state -> state is MediaPlayerState.Finished }

    /**
     * True if an error was encountered and the player was canceled
     */
    val isError: StateFlow<Boolean>
        get() = mediaPlayerState
            .map(viewModelScope) { state -> state is MediaPlayerState.Error }

    /**
     * Whether a TV show episode is currently playing, and it's not the last one in the season.
     */
    val hasNextEpisode: StateFlow<Boolean>
        get() = state.map(viewModelScope, State::hasNextEpisode)


    private fun playerStateToPlayButtonState(state: MediaPlayerState) = when (state) {
        is MediaPlayerState.Buffering -> PlayButtonState.HIDE
        is MediaPlayerState.Error -> PlayButtonState.HIDE
        is MediaPlayerState.Idle -> PlayButtonState.HIDE
        is MediaPlayerState.Paused -> PlayButtonState.SHOW_PLAY
        is MediaPlayerState.Playing -> PlayButtonState.SHOW_PAUSE
        is MediaPlayerState.Finished -> PlayButtonState.HIDE
    }
}