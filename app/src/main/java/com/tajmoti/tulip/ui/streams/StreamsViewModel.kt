package com.tajmoti.tulip.ui.streams

import androidx.lifecycle.*
import com.tajmoti.commonutils.statefulMap
import com.tajmoti.libtulip.model.info.StreamableInfo
import com.tajmoti.libtulip.model.key.StreamableKey
import com.tajmoti.libtulip.model.stream.StreamableInfoWithLangLinks
import com.tajmoti.libtulip.model.stream.UnloadedVideoStreamRef
import com.tajmoti.libtulip.repository.StreamsRepository
import com.tajmoti.libtulip.service.*
import com.tajmoti.libtvprovider.*
import com.tajmoti.tulip.ui.doCancelableJob
import com.tajmoti.tulip.ui.startFlowAction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class StreamsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val downloadService: VideoDownloadService,
    private val extractionService: StreamsRepository,
    private val streamService: LanguageMappingStreamService
) : ViewModel() {
    private val args = StreamsFragmentArgs.fromSavedStateHandle(savedStateHandle)
    private val _linkLoadingState = MutableStateFlow(false)
    private val _linkLoadingFlow = MutableSharedFlow<LinkLoadingState>()

    /**
     * Loading state of the video stream links.
     */
    val streamLoadingState: StateFlow<State> = startFlowAction<State>(State.Idle) {
        emitAll(fetchStreams(args.streamableKey))
    }

    /**
     * Name of the item, which the streams belong to.
     */
    val streamableName: StateFlow<String?> = streamLoadingState.map {
        val streamable = when (it) {
            is State.Loading -> it.info
            is State.Success -> it.infoFlow.value.info
            is State.Error -> it.info ?: return@map null
            else -> return@map null
        }
        streamableToName(streamable)
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    /**
     * Whether the stream links are being loaded right now.
     */
    val loading = streamLoadingState.statefulMap(viewModelScope)
    { it is State.Idle || it is State.Preparing || it is State.Loading }

    /**
     * True when loading is finished, but no streams were found. // TODO Better solution
     */
    val noResults = MutableStateFlow(false)

    /**
     * After the user clicks a link, this is the state of the link loading.
     */
    val linkLoadingState: Flow<LinkLoadingState> = _linkLoadingFlow

    /**
     * Whether a stream loading or direct link conversion is currently being performed.
     */
    val loadingStreamOrDirectLink = _linkLoadingState

    /**
     * Represents the link preparation and playing of the last clicked link.
     */
    private var linkExtractionJob: Job? = null

    /**
     * The user has clicked a link, it needs to be resolved and played.
     */
    fun onStreamClicked(stream: UnloadedVideoStreamRef, download: Boolean) {
        doCancelableJob(this::linkExtractionJob, _linkLoadingState) {
            val flow = when (val info = stream.info) {
                is VideoStreamRef.Resolved ->
                    processResolvedLink(stream, info, download)
                is VideoStreamRef.Unresolved ->
                    processUnresolvedLink(stream, info, download)
            }
            _linkLoadingFlow.emitAll(flow)
        }
    }

    /**
     * Converts a redirect to a streaming page to the actual URL
     * and passes it to [processResolvedLink].
     */
    private suspend fun processUnresolvedLink(
        ref: UnloadedVideoStreamRef,
        info: VideoStreamRef.Unresolved,
        download: Boolean
    ) = flow {
        emit(LinkLoadingState.Loading(info, download))
        val resolvedUrl = extractionService.resolveStream(info)
            .getOrElse { emit(LinkLoadingState.Error(info, download)); return@flow }
        emitAll(processResolvedLink(ref, resolvedUrl, download))
    }

    private suspend fun processResolvedLink(
        ref: UnloadedVideoStreamRef,
        info: VideoStreamRef.Resolved,
        download: Boolean,
    ) = flow {
        if (!ref.linkExtractionSupported) {
            emit(LinkLoadingState.DirectLinkUnsupported(info, download))
            return@flow
        }
        _linkLoadingFlow.emit(LinkLoadingState.LoadingDirect(info, download))
        val result = extractionService.extractVideoLink(info)
            .getOrElse {
                emit(LinkLoadingState.Error(info, download))
                return@flow
            }
        if (download)
            downloadVideo(result)
        emit(LinkLoadingState.LoadedDirect(info, download, result))
    }

    private fun downloadVideo(link: String) {
        val state = streamLoadingState.value as State.Success
        downloadService.downloadFileToFiles(link, state.infoFlow.value.info)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun fetchStreams(info: StreamableKey) = channelFlow {
        var loadingState: State.Loading? = null
        val consumer: (StreamableInfo) -> Unit = {
            val obj = State.Loading(it)
            loadingState = obj
            viewModelScope.launch { send(obj) }
        }
        val result = streamService.getStreamsWithLanguages(info, consumer)
            .map { it.onCompletion { updateAnyErrorsValue() } }
            .map { State.Success(it.stateIn(viewModelScope)) }
            .getOrElse { State.Error(loadingState?.info) }
        send(result)
    }

    private fun updateAnyErrorsValue() {
        val successState = (streamLoadingState.value as? State.Success)
        noResults.value = successState?.infoFlow?.value?.streams?.none() ?: false
    }

    private fun streamableToName(streamable: StreamableInfo): String {
        return when (streamable) {
            is StreamableInfo.Episode ->
                streamable.info.name ?: streamable.info.number.toString()
            is StreamableInfo.Movie -> streamable.name
        }
    }

    sealed class State {
        /**
         * Marker state before the loading is started.
         */
        object Idle : State()

        /**
         * Loading item information from the DB.
         */
        object Preparing : State()

        /**
         * Loading streams from the TV provider website.
         */
        data class Loading(val info: StreamableInfo) : State()

        /**
         * Streamable item loaded successfully.
         */
        data class Success(val infoFlow: StateFlow<StreamableInfoWithLangLinks>) : State()

        /**
         * Error during loading of the item.
         * Null if loading from the DB failed.
         */
        data class Error(val info: StreamableInfo?) : State()

        val success: Boolean
            get() = this is Success
    }

    sealed class LinkLoadingState {

        /**
         * The streaming page URL is being resolved.
         */
        data class Loading(
            val stream: VideoStreamRef.Unresolved,
            val download: Boolean
        ) : LinkLoadingState()

        /**
         * Direct link extraction is not supported for the clicked streaming site.
         */
        data class DirectLinkUnsupported(
            val stream: VideoStreamRef.Resolved,
            val download: Boolean
        ) : LinkLoadingState()

        /**
         * A direct video link is being extracted.
         */
        data class LoadingDirect(
            val stream: VideoStreamRef.Resolved,
            val download: Boolean
        ) : LinkLoadingState()

        /**
         * A direct video link was extracted successfully.
         */
        data class LoadedDirect(
            val stream: VideoStreamRef.Resolved,
            val download: Boolean,
            val directLink: String
        ) : LinkLoadingState()

        data class Error(
            val stream: VideoStreamRef,
            val download: Boolean
        ) : LinkLoadingState()
    }
}