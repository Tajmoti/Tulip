package com.tajmoti.tulip.ui.streams

import androidx.lifecycle.*
import com.tajmoti.commonutils.mapToAsyncJobs
import com.tajmoti.libtulip.model.info.StreamableInfo
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.StreamableKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.model.stream.UnloadedVideoStreamRef
import com.tajmoti.libtulip.model.stream.UnloadedVideoWithLanguage
import com.tajmoti.libtulip.service.HostedTvDataService
import com.tajmoti.libtulip.service.StreamExtractorService
import com.tajmoti.libtulip.service.TvDataService
import com.tajmoti.libtulip.service.VideoDownloadService
import com.tajmoti.libtvprovider.*
import com.tajmoti.tulip.ui.performStatefulOneshotOperation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject

@HiltViewModel
class StreamsViewModel @Inject constructor(
    private val hostedTvDataService: HostedTvDataService,
    private val downloadService: VideoDownloadService,
    private val extractionService: StreamExtractorService,
    private val tvDataService: TvDataService
) : ViewModel() {
    private val _state = MutableLiveData<State>(State.Idle)
    private val _linkLoadingState = MutableLiveData<LinkLoadingState>(LinkLoadingState.Idle)

    /**
     * Loading state of the video stream links.
     */
    val streamLoadingState: LiveData<State> = _state

    /**
     * Name of the item, which the streams belong to.
     */
    val streamableName: LiveData<String?> = Transformations.map(streamLoadingState) {
        val streamable = when (it) {
            is State.Loading -> it.info
            is State.Success -> it.info
            is State.Error -> it.info ?: return@map null
            else -> return@map null
        }
        streamableToName(streamable)
    }

    /**
     * Whether the stream links are being loaded right now.
     */
    val loading = Transformations.map(streamLoadingState)
    { it is State.Idle || it is State.Preparing || it is State.Loading }

    /**
     * True when loading is finished, but no streams were found.
     */
    val noResults = Transformations.map(streamLoadingState)
    { it is State.Success && it.streams.isEmpty() }

    /**
     * After the user clicks a link, this is the state of the link loading.
     */
    val linkLoadingState: LiveData<LinkLoadingState?> = _linkLoadingState

    /**
     * Whether a stream loading or direct link conversion is currently being performed.
     */
    val loadingStreamOrDirectLink = Transformations.map(_linkLoadingState) {
        it is LinkLoadingState.Loading || it is LinkLoadingState.LoadingDirect
    }

    /**
     * Represents the link preparation and playing of the last clicked link.
     */
    private var linkExtractionJob: Job? = null


    /**
     * Search for a TV show or a movie.
     */
    fun fetchStreams(info: StreamableKey) {
        performStatefulOneshotOperation(_state, State.Preparing, State.Idle) {
            fetchStreamsToState(info)
        }
    }

    /**
     * The user has clicked a link, it needs to be resolved and played.
     */
    fun onStreamClicked(stream: UnloadedVideoStreamRef, download: Boolean) {
        val info = stream.info
        linkExtractionJob?.cancel()
        linkExtractionJob = viewModelScope.launch {
            try {
                _linkLoadingState.value = when (info) {
                    is VideoStreamRef.Resolved ->
                        processResolvedLink(stream, info, download)
                    is VideoStreamRef.Unresolved ->
                        processUnresolvedLink(stream, info, download)
                }
            } catch (e: CancellationException) {
                _linkLoadingState.value = LinkLoadingState.Idle
            }
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
    ): LinkLoadingState {
        _linkLoadingState.value = LinkLoadingState.Loading(info, download)
        val resolvedUrl = extractionService.resolveStream(info)
            .getOrElse { return LinkLoadingState.Error }
        return processResolvedLink(ref, resolvedUrl, download)
    }

    private suspend fun processResolvedLink(
        ref: UnloadedVideoStreamRef,
        info: VideoStreamRef.Resolved,
        download: Boolean
    ): LinkLoadingState {
        if (!ref.linkExtractionSupported)
            return LinkLoadingState.DirectLinkUnsupported(info, download)
        _linkLoadingState.value = LinkLoadingState.LoadingDirect(info, download)
        val result = extractionService.extractVideoLink(info)
            .getOrElse { return LinkLoadingState.Error }
        if (download)
            downloadVideo(result)
        return LinkLoadingState.LoadedDirect(info, download, result)
    }

    private fun downloadVideo(link: String) {
        val state = streamLoadingState.value as State.Success
        downloadService.downloadFileToFiles(link, state.info)
    }

    private suspend fun fetchStreamsToState(info: StreamableKey): State {
        return when (info) {
            is StreamableKey.Hosted -> fetchStreamsToStateHosted(info)
            is StreamableKey.Tmdb -> fetchStreamsToStateTmdb(info)
        }
    }

    private suspend fun fetchStreams(info: StreamableKey.Hosted): Result<List<UnloadedVideoStreamRef>> {
        val result = extractionService.fetchStreams(info.streamingService, info.streamableKey)
            .getOrElse { return Result.failure(it) }
        return Result.success(result)
    }

    private suspend fun fetchStreamsToStateHosted(info: StreamableKey.Hosted): State {
        val newInfo = hostedTvDataService.getStreamableInfo(info)
            .getOrElse { return State.Error(null) }
        val result = fetchStreams(info)
            .getOrElse { return State.Error(newInfo.streamableInfo) }
            .map { UnloadedVideoWithLanguage(it, newInfo.language) }
        return State.Success(newInfo.streamableInfo, result)
    }

    private suspend fun fetchStreamsToStateTmdb(info: StreamableKey.Tmdb): State {
        val streamableInfo = tvDataService.getStreamableInfo(info)
            .getOrElse { return State.Error(null) }
        _state.value = State.Loading(streamableInfo)
        val streamables = when (info) {
            is MovieKey.Tmdb -> hostedTvDataService.getMovieByTmdbId(info)
                .map { it.map { it to it.language } }
                .getOrElse { return State.Error(null) }
            is EpisodeKey.Tmdb -> hostedTvDataService.getEpisodeByTmdbId(info)
                .map {
                    it.map { ep ->
                        val hostedKey = TvShowKey.Hosted(ep.service, ep.tvShowKey)
                        val show = hostedTvDataService.getTvShow(hostedKey)
                            .getOrElse { return State.Error(streamableInfo) }
                        ep to show.info.language
                    }
                }
                .getOrElse { return State.Error(null) }
        }
        val streams = mapToAsyncJobs(streamables) {
            fetchStreams(it.first.hostedKey)
                .getOrNull()
                ?.let { videos -> videos to it.second }
        }
            .filterNotNull()
            .flatMap { it.first.map { video -> video to it.second } }
            .map { UnloadedVideoWithLanguage(it.first, it.second) }
        return State.Success(streamableInfo, streams)
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
        data class Success(
            val info: StreamableInfo,
            val streams: List<UnloadedVideoWithLanguage>
        ) : State()

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
         * No link clicked yet.
         */
        object Idle : LinkLoadingState()

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

        object Error : LinkLoadingState()
    }
}