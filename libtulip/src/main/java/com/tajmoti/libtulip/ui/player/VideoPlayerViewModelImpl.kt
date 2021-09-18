package com.tajmoti.libtulip.ui.player

import com.tajmoti.commonutils.map
import com.tajmoti.libtulip.model.key.StreamableKey
import com.tajmoti.libtulip.model.subtitle.SubtitleInfo
import com.tajmoti.libtulip.repository.SubtitleRepository
import com.tajmoti.libtulip.service.*
import com.tajmoti.libtulip.ui.doCancelableJob
import com.tajmoti.libtvprovider.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File

class VideoPlayerViewModelImpl constructor(
    private val subtitleRepository: SubtitleRepository,
    private val subDirectory: File,
    private val viewModelScope: CoroutineScope,
    private val streamableKey: StreamableKey
) : VideoPlayerViewModel {

    /**
     * State of subtitle list loading
     */
    private val loadingSubtitlesState = MutableStateFlow<SubtitleListLoadingState>(
        SubtitleListLoadingState.Idle
    )

    /**
     * State of downloading of selected subtitles
     */
    private val subtitleDownloadState = MutableStateFlow<SubtitleDownloadingState>(
        SubtitleDownloadingState.Idle
    )

    override val subtitleOffset = MutableStateFlow(0L)
    override val loadingSubtitles = MutableStateFlow(false)
    override val downloadingSubtitleFile = MutableStateFlow(false)
    override val subtitleList = loadingSubtitlesState.map(viewModelScope) {
        (it as? SubtitleListLoadingState.Success)?.subtitles ?: emptyList()
    }
    override val subtitlesReadyToSelect = loadingSubtitlesState.map(viewModelScope) {
        it is SubtitleListLoadingState.Success
    }
    override val downloadingError = subtitleDownloadState.map(viewModelScope) {
        it is SubtitleDownloadingState.Error
    }
    override val subtitleFile = subtitleDownloadState.map(viewModelScope) {
        (it as? SubtitleDownloadingState.Success)?.subtitles
    }

    /**
     * State of the currently attached media player.
     */
    private val mediaPlayerState = MutableStateFlow<MediaPlayerHelper.State>(
        MediaPlayerHelper.State.Initializing
    )
    override val showPlayButton = mediaPlayerState.map(viewModelScope) {
        when (it) {
            is MediaPlayerHelper.State.Buffering -> VideoPlayerViewModel.PlayButtonState.HIDE
            is MediaPlayerHelper.State.Error -> VideoPlayerViewModel.PlayButtonState.HIDE
            is MediaPlayerHelper.State.Initializing -> VideoPlayerViewModel.PlayButtonState.HIDE
            is MediaPlayerHelper.State.Paused -> VideoPlayerViewModel.PlayButtonState.SHOW_PLAY
            is MediaPlayerHelper.State.Playing -> VideoPlayerViewModel.PlayButtonState.SHOW_PAUSE
        }
    }
    override val buffering = mediaPlayerState.map(viewModelScope) { state ->
        (state as? MediaPlayerHelper.State.Buffering)?.percent
    }
    override val position = mediaPlayerState.map(viewModelScope) { state ->
        when (state) {
            is MediaPlayerHelper.State.Buffering -> state.position
            is MediaPlayerHelper.State.Error -> null
            is MediaPlayerHelper.State.Initializing -> null
            is MediaPlayerHelper.State.Paused -> state.position
            is MediaPlayerHelper.State.Playing -> state.position
        }
    }
    override val lastValidPosition = position.mapNotNull { it?.timeMs }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0L)
    override val isError = mediaPlayerState.map(viewModelScope) { state ->
        state is MediaPlayerHelper.State.Error
    }

    /**
     * State of the interactive subtitle synchronization mechanism.
     * If not null, the user has pressed a button because they
     * saw a word that they want to match to a later shown subtitle text or vice versa.
     */
    private var subSyncState: SubtitleSyncState? = null

    /**
     * Represents fetching of the list of available subtitles.
     */
    private var subtitleFetchJob: Job? = null

    /**
     * Represents fetching of the selected subtitles.
     */
    private var subtitleDownloadJob: Job? = null

    init {
        loadSubtitleListIfNeeded()
    }

    private fun loadSubtitleListIfNeeded() {
        (streamableKey as? StreamableKey.Tmdb)?.let {
            viewModelScope.doCancelableJob(this::subtitleFetchJob, loadingSubtitles) {
                val subtitleListFlow = loadSubtitlesList(streamableKey)
                loadingSubtitlesState.emitAll(subtitleListFlow)
            }
        }
    }

    private fun loadSubtitlesList(id: StreamableKey.Tmdb) = flow {
        emit(SubtitleListLoadingState.Loading)
        val subtitles = subtitleRepository.fetchAvailableSubtitles(id)
            .getOrElse { emit(SubtitleListLoadingState.Error); return@flow }
        emit(SubtitleListLoadingState.Success(subtitles))
    }

    override fun onMediaAttached(media: MediaPlayerHelper) {
        viewModelScope.launch {
            mediaPlayerState.emitAll(media.state)
        }
        lastValidPosition.value
            .takeIf { it != 0L }
            ?.let { media.time = it }
    }

    override fun onSubtitlesSelected(subtitleInfo: SubtitleInfo) {
        subtitleOffset.value = 0L
        viewModelScope.doCancelableJob(this::subtitleDownloadJob, downloadingSubtitleFile) {
            subtitleDownloadState.emitAll(downloadSubtitles(subtitleInfo))
        }
    }

    override fun onWordHeard(time: Long) {
        val seen = subSyncState.takeIf { it is SubtitleSyncState.Seen }
                as? SubtitleSyncState.Seen
        if (seen == null) {
            subSyncState = SubtitleSyncState.Heard(time)
        } else {
            calculateAndSetSubtitleDelay(time, seen.time)
        }
    }

    override fun onTextSeen(time: Long) {
        // If an offset is already set, take it into account
        val adjustedTime = time - subtitleOffset.value
        val heard = subSyncState.takeIf { it is SubtitleSyncState.Heard }
                as? SubtitleSyncState.Heard
        if (heard == null) {
            subSyncState = SubtitleSyncState.Seen(adjustedTime)
        } else {
            calculateAndSetSubtitleDelay(heard.time, adjustedTime)
        }
    }

    private fun calculateAndSetSubtitleDelay(heardTime: Long, seenTime: Long) {
        subSyncState = null
        subtitleOffset.value = seenTime - heardTime
    }

    private fun downloadSubtitles(subtitleInfo: SubtitleInfo) = flow {
        emit(SubtitleDownloadingState.Loading)
        val subtitleStream = subtitleRepository.downloadSubtitleToFile(subtitleInfo, subDirectory)
            .getOrElse { emit(SubtitleDownloadingState.Error); return@flow }
        emit(SubtitleDownloadingState.Success(subtitleStream))
    }

    sealed interface SubtitleListLoadingState {
        object Idle : SubtitleListLoadingState
        object Loading : SubtitleListLoadingState
        data class Success(val subtitles: List<SubtitleInfo>) : SubtitleListLoadingState
        object Error : SubtitleListLoadingState
    }

    sealed interface SubtitleDownloadingState {
        object Idle : SubtitleDownloadingState
        object Loading : SubtitleDownloadingState
        data class Success(val subtitles: File) : SubtitleDownloadingState
        object Error : SubtitleDownloadingState
    }

    sealed interface SubtitleSyncState {
        class Heard(val time: Long) : SubtitleSyncState
        class Seen(val time: Long) : SubtitleSyncState
    }
}