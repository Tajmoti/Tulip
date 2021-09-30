package com.tajmoti.libtulip.ui.streams

import com.tajmoti.libtulip.model.info.StreamableInfo
import com.tajmoti.libtulip.model.key.StreamableKey
import com.tajmoti.libtulip.model.stream.StreamableInfoWithLangLinks
import com.tajmoti.libtulip.model.stream.UnloadedVideoStreamRef
import com.tajmoti.libtulip.repository.StreamsRepository
import com.tajmoti.libtulip.service.*
import com.tajmoti.libtvprovider.*
import com.tajmoti.libtvvideoextractor.CaptchaInfo
import com.tajmoti.libtvvideoextractor.ExtractionError
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class StreamsViewModelImpl constructor(
    private val downloadService: VideoDownloadService,
    private val streamsRepo: StreamsRepository,
    private val streamService: LanguageMappingStreamService,
    private val viewModelScope: CoroutineScope
) : StreamsViewModel {
    private val streamableKey = MutableSharedFlow<StreamableKey>()

    /**
     * Streamable key to loading state of the list of available streams.
     */
    private val streamLoadingStateWithKey = streamableKey
        .flatMapLatest { key -> fetchStreams(key).map { key to it } }
        .shareIn(viewModelScope, SharingStarted.Eagerly)

    /**
     * Loading state of the list of available streams
     */
    private val streamLoadingState = streamLoadingStateWithKey
        .map { (_, state) -> state }
        .stateIn(viewModelScope, SharingStarted.Eagerly, State.Idle)

    /**
     * Manually selected stream to play.
     */
    private val manualStream = MutableSharedFlow<Pair<UnloadedVideoStreamRef, Boolean>?>()

    /**
     * Auto-selected stream to play.
     */
    private val autoStream = streamLoadingStateWithKey
        .mapNotNull { (key, state) -> (state as? State.Success)?.let { key to state.streams } }
        .filter { (_, streams) -> anyGoodStreams(streams) }
        .distinctUntilChangedBy { (key, _) -> key }
        .map { (_, streams) -> firstGoodStream(streams).let { video -> video.video to false } }
        .shareIn(viewModelScope, SharingStarted.Lazily)

    private fun firstGoodStream(it: StreamableInfoWithLangLinks) =
        it.streams.first { video -> video.video.linkExtractionSupported }

    private fun anyGoodStreams(it: StreamableInfoWithLangLinks) =
        it.streams.any { s -> s.video.linkExtractionSupported }

    /**
     * The stream that should actually be played.
     */
    private val streamToPlay = merge(manualStream, autoStream)
        .shareIn(viewModelScope, SharingStarted.Lazily)

    /**
     * Loading state of a selected streaming service video
     */
    private val linkLoadingState = streamToPlay
        .flatMapLatest {
            it?.let { fetchStreams(it.first, it.second) }
                ?: flowOf(LinkLoadingState.Idle)
        }
        .shareIn(viewModelScope, SharingStarted.Lazily)

    override val streamableInfo = streamLoadingState
        .map { stateToStreamableInfo(it) }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)


    override val linksResult = streamLoadingState
        .map { (it as? State.Success)?.streams }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    override val linksAnyResult = MutableStateFlow(false)

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

    override val videoLinkToPlay = linkLoadingState
        .map { state ->
            (state as? LinkLoadingState.LoadedDirect)
                ?.takeIf { !it.download }
                ?.let { LoadedLink(it.stream, it.directLink) }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    override val videoLinkToDownload = linkLoadingState
        .map { state ->
            (state as? LinkLoadingState.LoadedDirect)
                ?.takeIf { it.download }
                ?.let { LoadedLink(it.stream, it.directLink) }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    override val linkLoadingError = linkLoadingState
        .mapNotNull { state ->
            (state as? LinkLoadingState.Error)
                ?.let { FailedLink(it.stream, it.download, it.captcha) }
        }
        .shareIn(viewModelScope, SharingStarted.Eagerly)


    override fun onStreamClicked(stream: UnloadedVideoStreamRef, download: Boolean) {
        viewModelScope.launch { manualStream.emit(stream to download) }
    }

    override fun onStreamClicked(key: StreamableKey) {
        viewModelScope.launch {
            manualStream.emit(null)
            streamableKey.emit(key)
        }
    }

    private fun fetchStreams(stream: UnloadedVideoStreamRef, download: Boolean) = flow {
        emit(LinkLoadingState.Idle)
        val flow = when (val info = stream.info) {
            is VideoStreamRef.Resolved ->
                processResolvedLink(stream, info, download)
            is VideoStreamRef.Unresolved ->
                processUnresolvedLink(stream, info, download)
        }
        emitAll(flow)
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
        streamsRepo.resolveStream(info)
            .onSuccess { emitAll(processResolvedLink(ref, it, download)) }
            .onFailure { emit(LinkLoadingState.Error(info, download, null)) }
    }

    private suspend fun processResolvedLink(
        ref: UnloadedVideoStreamRef,
        info: VideoStreamRef.Resolved,
        download: Boolean
    ) = flow {
        if (!ref.linkExtractionSupported) {
            emit(LinkLoadingState.DirectLinkUnsupported(info, download))
            return@flow
        }
        emit(LinkLoadingState.LoadingDirect(info, download))
        val result = streamsRepo.extractVideoLink(info)
            .map { result -> if (download) downloadVideo(result); result }
            .fold(
                { LinkLoadingState.Error(info, download, captchaOrNull(it)) },
                { LinkLoadingState.LoadedDirect(info, download, it) },
            )
        emit(result)
    }

    private fun captchaOrNull(it: ExtractionError) =
        (it as? ExtractionError.Captcha)?.info

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
        linksAnyResult.value = successState?.streams?.streams?.any() ?: false
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
            val download: Boolean,
            val captcha: CaptchaInfo?
        ) : LinkLoadingState
    }
}