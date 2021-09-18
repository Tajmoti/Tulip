package com.tajmoti.libtulip.ui.streams

import com.tajmoti.commonutils.map
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
        .stateIn(viewModelScope, SharingStarted.Eagerly, State.Idle)

    /**
     * Loading state of a selected streaming service video
     */
    private val linkLoadingState = MutableStateFlow<LinkLoadingState>(LinkLoadingState.Idle)


    override val linksLoading = streamLoadingState.map(viewModelScope) {
        it is State.Idle || it is State.Preparing || it is State.Loading
    }

    override val streamableName = streamLoadingState
        .map { stateToStreamName(it) }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)


    override val linksResult = streamLoadingState
        .map { (it as? State.Success)?.infoFlow }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    override val linksNoResult = MutableStateFlow(false)


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
        .shareIn(viewModelScope, SharingStarted.Eagerly)

    override val linkLoadingError = linkLoadingState
        .mapNotNull { state ->
            (state as? LinkLoadingState.Error)
                ?.let { FailedLink(it.stream, it.download) }
        }
        .shareIn(viewModelScope, SharingStarted.Eagerly)


    private var linkExtractionJob: Job? = null


    override fun onStreamClicked(stream: UnloadedVideoStreamRef, download: Boolean) {
        viewModelScope.doCancelableJob(
            this::linkExtractionJob,
            loadingStreamOrDirectLink
        ) {
            val flow = when (val info = stream.info) {
                is VideoStreamRef.Resolved ->
                    processResolvedLink(stream, info, download)
                is VideoStreamRef.Unresolved ->
                    processUnresolvedLink(stream, info, download)
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
        download: Boolean
    ) = flow {
        emit(LinkLoadingState.Loading(info, download))
        val resolvedUrl = streamsRepo.resolveStream(info)
            .getOrElse {
                emit(
                    LinkLoadingState.Error(
                        info,
                        download
                    )
                ); return@flow
            }
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
        linkLoadingState.emit(LinkLoadingState.LoadingDirect(info, download))
        val result = streamsRepo.extractVideoLink(info)
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
        downloadService.downloadFileToFiles(link, state.infoFlow.info)
    }

    private fun fetchStreams(info: StreamableKey) = flow {
        val result = streamService.getStreamsWithLanguages(info)
            .onCompletion { updateAnyErrorsValue() }
            .map { result -> result.fold({ State.Error(it) }, { State.Success(it) }) }
        emitAll(result)
    }

    private fun updateAnyErrorsValue() {
        val successState = (streamLoadingState.value as? State.Success)
        linksNoResult.value = successState?.infoFlow?.streams?.none() ?: false
    }

    private fun stateToStreamName(it: State): String? {
        val streamable = when (it) {
            is State.Loading -> it.info
            is State.Success -> it.infoFlow.info
            is State.Error -> it.info
            else -> null
        }
        return streamable?.let { streamableToName(it) }
    }

    private fun streamableToName(streamable: StreamableInfo): String {
        return when (streamable) {
            is StreamableInfo.Episode ->
                streamable.info.name ?: streamable.info.number.toString()
            is StreamableInfo.Movie -> streamable.name
        }
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
        data class Success(val infoFlow: StreamableInfoWithLangLinks) : State

        /**
         * Error during loading of the item.
         * Null if loading from the DB failed.
         */
        data class Error(val info: StreamableInfo?) : State

        val success: Boolean
            get() = this is Success
    }

    sealed interface LinkLoadingState {
        object Idle : LinkLoadingState

        /**
         * The streaming page URL is being resolved.
         */
        data class Loading(
            val stream: VideoStreamRef.Unresolved,
            val download: Boolean
        ) : LinkLoadingState

        /**
         * Direct link extraction is not supported for the clicked streaming site.
         */
        data class DirectLinkUnsupported(
            val stream: VideoStreamRef.Resolved,
            val download: Boolean
        ) : LinkLoadingState

        /**
         * A direct video link is being extracted.
         */
        data class LoadingDirect(
            val stream: VideoStreamRef.Resolved,
            val download: Boolean
        ) : LinkLoadingState

        /**
         * A direct video link was extracted successfully.
         */
        data class LoadedDirect(
            val stream: VideoStreamRef.Resolved,
            val download: Boolean,
            val directLink: String
        ) : LinkLoadingState

        data class Error(
            val stream: VideoStreamRef,
            val download: Boolean
        ) : LinkLoadingState
    }
}