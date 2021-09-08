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
class VideoPlayerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val subtitleRepository: SubtitleRepository,
    @ApplicationContext
    private val context: Context
) : ViewModel() {
    private val args = VideoPlayerActivityArgs.fromSavedStateHandle(savedStateHandle)

    private val _subtitleOffset = MutableStateFlow(0L)
    private val _subtitleListLoadingState = MutableStateFlow(false)
    private val _subtitleListLoadingFlow =
        MutableStateFlow<SubtitleListLoadingState>(SubtitleListLoadingState.Loading)
    private val _subtitleDownloadState = MutableStateFlow(false)
    private val _subtitleDownloadFlow =
        MutableStateFlow<SubtitleDownloadingState>(SubtitleDownloadingState.Idle)

    /**
     * Result of subtitle loading.
     */
    private val loadingSubtitlesState: StateFlow<SubtitleListLoadingState> =
        _subtitleListLoadingFlow

    /**
     * Successful result of subtitle loading.
     */
    val subtitleList: StateFlow<List<SubtitleInfo>> = loadingSubtitlesState.map(viewModelScope) {
        when (it) {
            is SubtitleListLoadingState.Success -> it.subtitles
            else -> emptyList()
        }
    }

    /**
     * Whether the list of available subtitles is being loaded right now.
     */
    val loadingSubtitles: StateFlow<Boolean> = _subtitleListLoadingState

    /**
     * Whether subtitles are loaded and can be selected.
     */
    val subtitlesReadyToSelect: StateFlow<Boolean> = loadingSubtitlesState.map(viewModelScope) {
        it is SubtitleListLoadingState.Success
    }

    /**
     * Whether some subtitle file is being downloaded right now.
     */
    val downloadingSubtitleFile: StateFlow<Boolean> = _subtitleDownloadState

    /**
     * Whether there was some error while downloading the subtitles.
     */
    val downloadingError: StateFlow<Boolean> = _subtitleDownloadFlow.map(viewModelScope) {
        it is SubtitleDownloadingState.Error
    }

    /**
     * Successful result of subtitle loading.
     */
    val subtitleFile: StateFlow<File?> = _subtitleDownloadFlow.map(viewModelScope) {
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
     * How much the subtitles should be offset.
     * Positive values mean subtitles should be shown earlier.
     */
    val subtitleOffset: StateFlow<Long> = _subtitleOffset

    /**
     * Represents fetching of the list of available subtitles.
     */
    private var subtitleFetchJob: Job? = null

    /**
     * Represents fetching of the selected subtitles.
     */
    private var subtitleDownloadJob: Job? = null

    init {
        doCancelableJob(this::subtitleFetchJob, _subtitleListLoadingState) {
            _subtitleListLoadingFlow.emitAll(loadSubtitlesList(args.streamableKey as StreamableKey.Tmdb))
        }
    }

    private fun loadSubtitlesList(id: StreamableKey.Tmdb) = flow {
        emit(SubtitleListLoadingState.Loading)
        val subtitles = subtitleRepository.fetchAvailableSubtitles(id)
            .getOrElse { emit(SubtitleListLoadingState.Error); return@flow }
        emit(SubtitleListLoadingState.Success(subtitles))
    }

    /**
     * The user has selected which subtitles they wish to use.
     * The subtitles need to be downloaded before the video is played.
     */
    fun onSubtitlesSelected(subtitleInfo: SubtitleInfo) {
        _subtitleOffset.value = 0L
        doCancelableJob(this::subtitleDownloadJob, _subtitleDownloadState) {
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
        _subtitleOffset.value = seenTime - heardTime
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
        class Heard(val time: Long): SubtitleSyncState
        class Seen(val time: Long): SubtitleSyncState
    }
}