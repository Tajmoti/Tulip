package com.tajmoti.libtulip.ui.player

import com.tajmoti.commonutils.map
import com.tajmoti.libtulip.dto.StreamableInfoDto
import com.tajmoti.libtulip.dto.StreamingSiteLinkDto
import com.tajmoti.libtulip.dto.SubtitleDto
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.StreamableKey
import com.tajmoti.libtulip.model.key.SubtitleKey
import com.tajmoti.libtulip.ui.StateViewModel
import com.tajmoti.libtulip.ui.streams.FailedLink
import com.tajmoti.libtulip.ui.streams.LoadedLink
import com.tajmoti.libtulip.ui.streams.SelectedLink
import kotlinx.coroutines.flow.*

interface VideoPlayerViewModel : StateViewModel<VideoPlayerViewModel.State> {

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
    fun onStreamClicked(stream: StreamingSiteLinkDto, download: Boolean)


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
    fun onSubtitlesSelected(key: SubtitleKey?)

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


    data class State(
        /**
         * If this is a TV show, this is its information.
         */
        val tvShowData: TvShowData? = null,
        /**
         * Key of the streamable that is currently being loaded or played.
         */
        val streamableKey: StreamableKey?,
        /**
         * Streamable info of [streamableKey] or null if not yet available.
         */
        val streamableInfo: StreamableInfoDto?,

        /**
         * State of link list loading.
         */
        val linkListState: LinkListState,
        /**
         * State of selected link loading.
         */
        val selectedLinkState: SelectedLinkState,
        /**
         * State of playback.
         */
        val playbackState: PlaybackState,
        /**
         * State of subtitle loading.
         */
        val subtitleState: SubtitleState
    )

    data class LinkListState(
        /**
         * Whether stream links are being loaded right now and there are no loaded links yet.
         */
        val showNoLinksYetLoadingProgress: Boolean,
        /**
         * Whether each stream link producer produced at least one result
         * and the loaded list of links can be considered final.
         */
        val showLinksStillLoadingProgress: Boolean,
        /**
         * Loaded links of the currently selected [State.streamableKey] or null if not yet available.
         * This value is updated in real time as more links are loaded in.
         */
        val linksResult: List<StreamingSiteLinkDto>?,
        /**
         * Whether link list loading is finished and at least one stream was found.
         */
        val linksAnyResult: Boolean,
        /**
         * Whether link list loading is finished, but no streams were found.
         */
        val linksNoResult: Boolean,
    )

    data class SelectedLinkState(
        /**
         * Whether a direct link is being loaded right now.
         */
        val showSelectedLinkLoadingProgress: Boolean,
        /**
         * Link that was selected for playback. It might be loading, be already loaded, or errored out.
         */
        val videoLinkPreparingOrPlaying: StreamingSiteLinkDto?,
        /**
         * Selected item with direct link loaded.
         */
        val videoLinkToPlay: LoadedLink?,
        /**
         * Selected item for playing is loaded
         */
        val videoLinkToDownload: LoadedLink?,
        /**
         * Selected item failed to load redirects or failed to load direct link.
         */
        val linkLoadingError: FailedLink?,
        /**
         * Selected item which has redirects resolved, but doesn't support direct link loading.
         */
        val directLoadingUnsupported: SelectedLink?
    )

    data class PlaybackState(
        /**
         * How much the subtitles should be offset.
         * Positive values mean that subtitles must be delayed,
         * negative values mean they need to be shown earlier.
         */
        val subtitleOffset: Long,
        /**
         * If true show play, if false show pause, if null don't show at all
         */
        val showPlayButton: PlayButtonState,
        /**
         * True if the video is currently playing
         */
        val isPlaying: Boolean,
        /**
         * Buffering progress or null if not buffering
         */
        val buffering: Float?,
        /**
         * Playing position or null if position should be hidden
         */
        val position: Position?,
        /**
         * State of the playback.
         */
        val mediaPlayerState: MediaPlayerState,
        /**
         * Whether the media has successfully played until the end
         */
        val isDonePlaying: Boolean,
        /**
         * True if an error was encountered and the player was canceled
         */
        val isError: Boolean,
    )

    data class SubtitleState(
        /**
         * Successful result of subtitle loading.
         */
        val subtitleList: List<SubtitleDto>,

        /**
         * Whether the list of available subtitles is being loaded right now.
         */
        val loadingSubtitleList: Boolean,

        /**
         * Whether subtitles are loaded and can be selected.
         */
        val subtitlesReadyToSelect: Boolean,

        /**
         * Whether some subtitles file is being downloaded right now.
         */
        val downloadingSubtitles: Boolean,

        /**
         * Whether there was some error while downloading the selected subtitle file.
         */
        val subtitleDownloadError: Boolean,

        /**
         * Subtitles to be applied to the currently playing video
         */
        val subtitleFile: String?,
    )

    data class TvShowData(
        val previousEpisode: EpisodeKey? = null,
        val nextEpisode: EpisodeKey? = null,
    )

    /**
     * True if the currently playing streamable is a TV show, false if it is a movie.
     */
    val isTvShow: StateFlow<Boolean>
        get() = state.map(viewModelScope) { it.tvShowData != null }

    /**
     * Whether stream links are being loaded right now and there are no loaded links yet.
     */
    val showNoLinksYetLoadingProgress: StateFlow<Boolean>
        get() = state.map(viewModelScope) { it.linkListState.showNoLinksYetLoadingProgress }

    /**
     * Key of the streamable that is currently being loaded or played.
     */
    val streamableKey: StateFlow<StreamableKey?>
        get() = state.map(viewModelScope, State::streamableKey)

    /**
     * Streamable info of [streamableKey] or null if not yet available.
     */
    val streamableInfo: StateFlow<StreamableInfoDto?>
        get() = state.map(viewModelScope, State::streamableInfo)

    /**
     * Loaded links of the currently selected [streamableKey] or null if not yet available.
     * This value is updated in real time as more links are loaded in.
     */
    val linksResult: StateFlow<List<StreamingSiteLinkDto>?>
        get() = state.map(viewModelScope) { it.linkListState.linksResult }

    /**
     * Whether link list loading is finished and at least one stream was found.
     */
    val linksAnyResult: StateFlow<Boolean>
        get() = state.map(viewModelScope) { it.linkListState.linksAnyResult }

    /**
     * Whether link list loading is finished, but no streams were found.
     */
    val linksNoResult: StateFlow<Boolean>
        get() = state.map(viewModelScope) { it.linkListState.linksNoResult }


    /**
     * Whether a direct link is being loaded right now.
     */
    val showSelectedLinkLoadingProgress: StateFlow<Boolean>
        get() = state.map(viewModelScope) { it.selectedLinkState.showSelectedLinkLoadingProgress }

    /**
     * Selected item which has redirects resolved, but doesn't support direct link loading.
     */
    val directLoadingUnsupported: SharedFlow<SelectedLink>
        get() = state.mapNotNull { it.selectedLinkState.directLoadingUnsupported }
            .shareIn(viewModelScope, SharingStarted.Eagerly)

    /**
     * Link that was selected for playback. It might be loading, be already loaded, or errored out.
     */
    val videoLinkPreparingOrPlaying: StateFlow<StreamingSiteLinkDto?>
        get() = state.map(viewModelScope) { it.selectedLinkState.videoLinkPreparingOrPlaying }

    /**
     * Selected item with direct link loaded.
     */
    val videoLinkToPlay: StateFlow<LoadedLink?>
        get() = state.map(viewModelScope) { it.selectedLinkState.videoLinkToPlay }

    /**
     * Selected item for playing is loaded
     */
    val videoLinkToDownload: StateFlow<LoadedLink?>
        get() = state.map(viewModelScope) { it.selectedLinkState.videoLinkToDownload }

    /**
     * Selected item failed to load redirects or failed to load direct link.
     */
    val linkLoadingError: SharedFlow<FailedLink>
        get() = state.mapNotNull { it.selectedLinkState.linkLoadingError }
            .shareIn(viewModelScope, SharingStarted.Eagerly)


    /**
     * Successful result of subtitle loading.
     */
    val subtitleList: StateFlow<List<SubtitleDto>>
        get() = state.map(viewModelScope) { it.subtitleState.subtitleList }

    /**
     * Whether the list of available subtitles is being loaded right now.
     */
    val loadingSubtitleList: StateFlow<Boolean>
        get() = state.map(viewModelScope) { it.subtitleState.loadingSubtitleList }

    /**
     * Whether subtitles are loaded and can be selected.
     */
    val subtitlesReadyToSelect: StateFlow<Boolean>
        get() = state.map(viewModelScope) { it.subtitleState.subtitlesReadyToSelect }

    /**
     * Whether some subtitles file is being downloaded right now.
     */
    val downloadingSubtitles: StateFlow<Boolean>
        get() = state.map(viewModelScope) { it.subtitleState.downloadingSubtitles }

    /**
     * Whether there was some error while downloading the selected subtitle file.
     */
    val subtitleDownloadError: StateFlow<Boolean>
        get() = state.map(viewModelScope) { it.subtitleState.subtitleDownloadError }

    /**
     * Subtitles to be applied to the currently playing video
     */
    val subtitleFile: StateFlow<String?>
        get() = state.map(viewModelScope) { it.subtitleState.subtitleFile }

    /**
     * How much the subtitles should be offset.
     * Positive values mean that subtitles must be delayed,
     * negative values mean they need to be shown earlier.
     */
    val subtitleOffset: StateFlow<Long>
        get() = state.map(viewModelScope) { it.playbackState.subtitleOffset }

    /**
     * If true show play, if false show pause, if null don't show at all
     */
    val showPlayButton: StateFlow<PlayButtonState>
        get() = state.map(viewModelScope) { it.playbackState.showPlayButton }


    /**
     * State of the playback.
     */
    val mediaPlayerState: StateFlow<MediaPlayerState>
        get() = state.map(viewModelScope) { it.playbackState.mediaPlayerState }

    /**
     * True if the video is currently playing
     */
    val isPlaying: StateFlow<Boolean>
        get() = state.map(viewModelScope) { it.playbackState.isPlaying }

    /**
     * Buffering progress or null if not buffering
     */
    val buffering: StateFlow<Float?>
        get() = state.map(viewModelScope) { it.playbackState.buffering }

    /**
     * Playing position or null if position should be hidden
     */
    val position: StateFlow<Position?>
        get() = state.map(viewModelScope) { it.playbackState.position }

    /**
     * Whether the media has successfully played until the end
     */
    val isDonePlaying: StateFlow<Boolean>
        get() = state.map(viewModelScope) { it.playbackState.isDonePlaying }

    /**
     * True if an error was encountered and the player was canceled
     */
    val isError: StateFlow<Boolean>
        get() = state.map(viewModelScope) { it.playbackState.isError }

    /**
     * Whether a TV show episode is currently playing, and it's not the last one in the season.
     */
    val hasNextEpisode: StateFlow<Boolean>
        get() = state.map(viewModelScope) { it.tvShowData?.nextEpisode != null }
}