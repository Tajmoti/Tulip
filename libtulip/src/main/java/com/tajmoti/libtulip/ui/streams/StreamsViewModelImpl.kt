package com.tajmoti.libtulip.ui.streams

import com.tajmoti.libtulip.model.info.StreamableInfo
import com.tajmoti.libtulip.model.key.StreamableKey
import com.tajmoti.libtulip.model.stream.StreamableInfoWithLangLinks
import com.tajmoti.libtulip.model.stream.UnloadedVideoStreamRef
import com.tajmoti.libtulip.repository.StreamsRepository
import com.tajmoti.libtulip.service.*
import com.tajmoti.libtulip.ui.doCancelableJob
import com.tajmoti.libtvprovider.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class StreamsViewModelImpl constructor(
    private val downloadService: VideoDownloadService,
    private val streamsRepo: StreamsRepository,
    private val streamService: LanguageMappingStreamService,
    streamableKey: StreamableKey,
    private val viewModelScope: CoroutineScope
) : StreamsViewModel {
    /**
     * Loading state of the list of available streams
     */
    private val streamLoadingState = fetchStreams(streamableKey)
        .onEach { (it as? State.Success)?.let { success -> onInitialStreams(success.streams) } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, State.Idle)

    /**
     * Loading state of a selected streaming service video
     */
    private val linkLoadingState = MutableStateFlow<LinkLoadingState>(LinkLoadingState.Idle)

    override val streamableInfo = streamLoadingState
        .map { stateToStreamableInfo(it) }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)


    override val linksResult = streamLoadingState
        .map { (it as? State.Success)?.streams }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    override val linksNoResult = MutableStateFlow(false)

    override val linksLoading = streamLoadingState
        .combine(linksNoResult) { state, noResults ->
            state is State.Idle
                    || state is State.Preparing
                    || state is State.Loading
                    || (state is State.Success && state.streams.streams.isEmpty() && !noResults)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)


    override val loadingStreamOrDirectLink = MutableStateFlow(false)

    override val directLoadingUnsupported = linkLoadingState
        .mapNotNull { state ->
            (state as? LinkLoadingState.DirectLinkUnsupported)
                ?.let { SelectedLink(it.stream, it.download) }
        }
        .shareIn(viewModelScope, SharingStarted.Eagerly)

    override val directLoaded = linkLoadingState
        .mapNotNull { state ->
            (state as? LinkLoadingState.LoadedDirect)
                ?.let { LoadedLink(it.stream, it.download, it.directLink) }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    override val linkLoadingError = linkLoadingState
        .mapNotNull { state ->
            (state as? LinkLoadingState.Error)
                ?.let { FailedLink(it.stream, it.download) }
        }
        .shareIn(viewModelScope, SharingStarted.Eagerly)

    /**
     * True if a stream was already auto-selected for automatic play.
     */
    private var autoStreamAlreadySelected = false

    /**
     * A job representing ongoing link extraction.
     * To be canceled if another video stream is selected.
     */
    private var linkExtractionJob: Job? = null


    override fun onStreamClicked(stream: UnloadedVideoStreamRef, download: Boolean) {
        startFetchingStream(stream, download, false)
    }

    private fun startFetchingStream(
        stream: UnloadedVideoStreamRef,
        download: Boolean,
        auto: Boolean
    ) {
        viewModelScope.doCancelableJob(
            this::linkExtractionJob,
            loadingStreamOrDirectLink
        ) {
            val flow = when (val info = stream.info) {
                is VideoStreamRef.Resolved ->
                    processResolvedLink(stream, info, download, auto)
                is VideoStreamRef.Unresolved ->
                    processUnresolvedLink(stream, info, download, auto)
            }
            linkLoadingState.emitAll(flow)
        }
    }

    /**
     * Converts a redirect to a streaming page to the actual URL
     * and passes it to [processResolvedLink].
     */
    private suspend fun processUnresolvedLink(
        ref: UnloadedVideoStreamRef,
        info: VideoStreamRef.Unresolved,
        download: Boolean,
        auto: Boolean
    ) = flow {
        emit(LinkLoadingState.Loading(info, download, auto))
        streamsRepo.resolveStream(info)
            .onSuccess { emitAll(processResolvedLink(ref, it, download, auto)) }
            .onFailure { emit(LinkLoadingState.Error(info, download, auto)) }
    }

    private suspend fun processResolvedLink(
        ref: UnloadedVideoStreamRef,
        info: VideoStreamRef.Resolved,
        download: Boolean,
        auto: Boolean
    ) = flow {
        if (!ref.linkExtractionSupported) {
            emit(LinkLoadingState.DirectLinkUnsupported(info, download, auto))
            return@flow
        }
        emit(LinkLoadingState.LoadingDirect(info, download, auto))
        val result = streamsRepo.extractVideoLink(info)
            .onSuccess { result -> if (download) downloadVideo(result) }
            .fold(
                { LinkLoadingState.LoadedDirect(info, download, it, auto) },
                { LinkLoadingState.Error(info, download, auto) }
            )
        emit(result)
    }

    private fun downloadVideo(link: String) {
        val state = streamLoadingState.value as State.Success
        downloadService.downloadFileToFiles(link, state.streams.info)
    }

    private fun fetchStreams(info: StreamableKey) = flow {
        val result = streamService.getStreamsWithLanguages(info)
            .onCompletion { updateAnyErrorsValue() }
            .map { result -> result.fold({ State.Error(it) }, { State.Success(it) }) }
        emitAll(result)
    }

    private fun updateAnyErrorsValue() {
        val successState = (streamLoadingState.value as? State.Success)
        linksNoResult.value = successState?.streams?.streams?.none() ?: false
    }

    private fun stateToStreamableInfo(it: State): StreamableInfo? {
        return when (it) {
            is State.Loading -> it.info
            is State.Success -> it.streams.info
            is State.Error -> it.info
            else -> null
        }
    }

    private fun onInitialStreams(info: StreamableInfoWithLangLinks) {
        if (autoStreamAlreadySelected)
            return
        val firstDirect = info.streams.firstOrNull { it.video.linkExtractionSupported } ?: return
        autoStreamAlreadySelected = true
        startFetchingStream(firstDirect.video, download = false, auto = true)
    }


    sealed interface State {
        /**
         * Marker state before the loading is started.
         */
        object Idle : State

        /**
         * Loading item information from the DB.
         */
        object Preparing : State

        /**
         * Loading streams from the TV provider website.
         */
        data class Loading(val info: StreamableInfo) : State

        /**
         * Streamable item loaded successfully.
         */
        data class Success(val streams: StreamableInfoWithLangLinks) : State

        /**
         * Error during loading of the item.
         * Null if loading from the DB failed.
         */
        data class Error(val info: StreamableInfo?) : State

        val success: Boolean
            get() = this is Success
    }

    sealed interface LinkLoadingState {
        val auto: Boolean

        object Idle : LinkLoadingState {
            override val auto = false
        }

        /**
         * The streaming page URL is being resolved.
         */
        data class Loading(
            val stream: VideoStreamRef.Unresolved,
            val download: Boolean,
            override val auto: Boolean
        ) : LinkLoadingState

        /**
         * Direct link extraction is not supported for the clicked streaming site.
         */
        data class DirectLinkUnsupported(
            val stream: VideoStreamRef.Resolved,
            val download: Boolean,
            override val auto: Boolean
        ) : LinkLoadingState

        /**
         * A direct video link is being extracted.
         */
        data class LoadingDirect(
            val stream: VideoStreamRef.Resolved,
            val download: Boolean,
            override val auto: Boolean
        ) : LinkLoadingState

        /**
         * A direct video link was extracted successfully.
         */
        data class LoadedDirect(
            val stream: VideoStreamRef.Resolved,
            val download: Boolean,
            val directLink: String,
            override val auto: Boolean
        ) : LinkLoadingState

        data class Error(
            val stream: VideoStreamRef,
            val download: Boolean,
            override val auto: Boolean
        ) : LinkLoadingState
    }
}