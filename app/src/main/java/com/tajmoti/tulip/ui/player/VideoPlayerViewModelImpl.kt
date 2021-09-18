package com.tajmoti.tulip.ui.player

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.*
import com.tajmoti.commonutils.map
import com.tajmoti.libtulip.model.key.StreamableKey
import com.tajmoti.libtulip.model.subtitle.SubtitleInfo
import com.tajmoti.libtulip.repository.SubtitleRepository
import com.tajmoti.libtulip.service.*
import com.tajmoti.libtvprovider.*
import com.tajmoti.tulip.ui.doCancelableJob
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import javax.inject.Inject

@HiltViewModel
@SuppressLint("StaticFieldLeak")
class VideoPlayerViewModelImpl @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val subtitleRepository: SubtitleRepository,
    @ApplicationContext
    private val context: Context
) : ViewModel(), VideoPlayerViewModel {
    private val args = VideoPlayerActivityArgs.fromSavedStateHandle(savedStateHandle)

    /**
     * State of subtitle list loading
     */
    private val loadingSubtitlesState = MutableStateFlow<SubtitleListLoadingState>(
        SubtitleListLoadingState.Loading
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
        doCancelableJob(this::subtitleFetchJob, loadingSubtitles) {
            val subtitleListFlow = loadSubtitlesList(args.streamableKey as StreamableKey.Tmdb)
            loadingSubtitlesState.emitAll(subtitleListFlow)
        }
    }

    private fun loadSubtitlesList(id: StreamableKey.Tmdb) = flow {
        emit(SubtitleListLoadingState.Loading)
        val subtitles = subtitleRepository.fetchAvailableSubtitles(id)
            .getOrElse { emit(SubtitleListLoadingState.Error); return@flow }
        emit(SubtitleListLoadingState.Success(subtitles))
    }

    /**
     * A new media is attached and starting to be played.
     */
    fun onMediaAttached(media: MediaPlayerHelper) {
        viewModelScope.launch {
            mediaPlayerState.emitAll(media.state)
        }
    }

    /**
     * The user has selected which subtitles they wish to use.
     * The subtitles need to be downloaded before the video is played.
     */
    fun onSubtitlesSelected(subtitleInfo: SubtitleInfo) {
        subtitleOffset.value = 0L
        doCancelableJob(this::subtitleDownloadJob, downloadingSubtitleFile) {
            subtitleDownloadState.emitAll(downloadSubtitles(subtitleInfo))
        }
    }

    /**
     * The user heard a word that they want to match to some text.
     */
    fun onWordHeard(time: Long) {
        val seen = subSyncState.takeIf { it is SubtitleSyncState.Seen }
                as? SubtitleSyncState.Seen
        if (seen == null) {
            subSyncState = SubtitleSyncState.Heard(time)
        } else {
            calculateAndSetSubtitleDelay(time, seen.time)
        }
    }

    /**
     * The user saw text that they want to match to a heard word.
     */
    fun onTextSeen(time: Long) {
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
        val subDirectory = context.getExternalFilesDir(null)!!
        val subtitleStream = subtitleRepository.downloadSubtitleToFile(subtitleInfo, subDirectory)
            .getOrElse { emit(SubtitleDownloadingState.Error); return@flow }
        emit(SubtitleDownloadingState.Success(subtitleStream))
    }

    sealed interface SubtitleListLoadingState {
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