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
    override val subtitleOffset = MutableStateFlow(0L)
    private val _subtitleListLoadingState = MutableStateFlow(false)
    private val loadingSubtitlesState =
        MutableStateFlow<SubtitleListLoadingState>(SubtitleListLoadingState.Loading)
    override val downloadingSubtitleFile = MutableStateFlow(false)
    private val _subtitleDownloadFlow =
        MutableStateFlow<SubtitleDownloadingState>(SubtitleDownloadingState.Idle)
    override val subtitleList = loadingSubtitlesState.map(viewModelScope) {
        when (it) {
            is SubtitleListLoadingState.Success -> it.subtitles
            else -> emptyList()
        }
    }
    override val loadingSubtitles = _subtitleListLoadingState
    override val subtitlesReadyToSelect = loadingSubtitlesState.map(viewModelScope) {
        it is SubtitleListLoadingState.Success
    }
    override val downloadingError = _subtitleDownloadFlow.map(viewModelScope) {
        it is SubtitleDownloadingState.Error
    }
    override val subtitleFile = _subtitleDownloadFlow.map(viewModelScope) {
        when (it) {
            is SubtitleDownloadingState.Success -> it.subtitles
            else -> null
        }
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

    override val showPlayButton = MutableStateFlow(VideoPlayerViewModel.PlayButtonState.HIDE)
    override val buffering = MutableStateFlow<Float?>(null)
    override val isError = MutableStateFlow(false)
    override val position = MutableStateFlow<Position?>(null)

    init {
        doCancelableJob(this::subtitleFetchJob, _subtitleListLoadingState) {
            loadingSubtitlesState.emitAll(loadSubtitlesList(args.streamableKey as StreamableKey.Tmdb))
        }
    }

    private fun loadSubtitlesList(id: StreamableKey.Tmdb) = flow {
        emit(SubtitleListLoadingState.Loading)
        val subtitles = subtitleRepository.fetchAvailableSubtitles(id)
            .getOrElse { emit(SubtitleListLoadingState.Error); return@flow }
        emit(SubtitleListLoadingState.Success(subtitles))
    }

    fun onMediaStateChanged(state: MediaPlayerHelper.State) {
        when (state) {
            is MediaPlayerHelper.State.Buffering -> {
                showPlayButton.value = VideoPlayerViewModel.PlayButtonState.HIDE
                position.value = state.position
                buffering.value = state.percent
                isError.value = false
            }
            is MediaPlayerHelper.State.Error -> {
                showPlayButton.value = VideoPlayerViewModel.PlayButtonState.HIDE
                position.value = null
                buffering.value = null
                isError.value = true
            }
            is MediaPlayerHelper.State.Initializing -> {
                showPlayButton.value = VideoPlayerViewModel.PlayButtonState.HIDE
                position.value = null
                buffering.value = null
                isError.value = false
            }
            is MediaPlayerHelper.State.Paused -> {
                showPlayButton.value = VideoPlayerViewModel.PlayButtonState.SHOW_PLAY
                position.value = state.position
                buffering.value = null
                isError.value = false
            }
            is MediaPlayerHelper.State.Playing -> {
                showPlayButton.value = VideoPlayerViewModel.PlayButtonState.SHOW_PAUSE
                position.value = state.position
                buffering.value = null
                isError.value = false
            }
        }
    }

    /**
     * The user has selected which subtitles they wish to use.
     * The subtitles need to be downloaded before the video is played.
     */
    fun onSubtitlesSelected(subtitleInfo: SubtitleInfo) {
        subtitleOffset.value = 0L
        doCancelableJob(
            this::subtitleDownloadJob,
            downloadingSubtitleFile
        ) {
            _subtitleDownloadFlow.emitAll(downloadSubtitles(subtitleInfo))
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

    sealed class SubtitleListLoadingState {
        object Loading : SubtitleListLoadingState()

        data class Success(
            val subtitles: List<SubtitleInfo>
        ) : SubtitleListLoadingState()

        object Error : SubtitleListLoadingState()
    }

    sealed class SubtitleDownloadingState {
        object Idle : SubtitleDownloadingState()

        object Loading : SubtitleDownloadingState()

        data class Success(val subtitles: File) : SubtitleDownloadingState()

        object Error : SubtitleDownloadingState()
    }

    sealed interface SubtitleSyncState {
        class Heard(val time: Long) : SubtitleSyncState
        class Seen(val time: Long) : SubtitleSyncState
    }
}