package com.tajmoti.tulip.ui.streams

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.tajmoti.libtvprovider.Episode
import com.tajmoti.libtvprovider.Streamable
import com.tajmoti.libtvprovider.TvItem
import com.tajmoti.libtvprovider.VideoStreamRef
import com.tajmoti.libtvvideoextractor.VideoLinkExtractor
import com.tajmoti.tulip.model.StreamableInfo
import com.tajmoti.tulip.model.key.StreamableKey
import com.tajmoti.tulip.service.StreamExtractorService
import com.tajmoti.tulip.service.TvDataService
import com.tajmoti.tulip.service.VideoDownloadService
import com.tajmoti.tulip.ui.performStatefulOneshotOperation
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StreamsViewModel @Inject constructor(
    private val tvDataService: TvDataService,
    private val linkExtractor: VideoLinkExtractor,
    private val downloadService: VideoDownloadService,
    private val extractionService: StreamExtractorService
) : ViewModel() {
    private val _state = MutableLiveData<State>(State.Idle)
    private val _streamableName = MutableLiveData<String?>()
    private val _directLoadingState = MutableLiveData<DirectStreamLoading?>()

    /**
     * Loading state of the video stream links.
     */
    val streamLoadingState: LiveData<State> = _state

    /**
     * Name of the item, which the streams belong to.
     */
    val streamableName: LiveData<String?> = _streamableName

    /**
     * Whether the stream links are being loaded right now.
     */
    val loading = Transformations.map(streamLoadingState)
    { it is State.Loading }

    /**
     * True when loading is finished, but no streams were found.
     */
    val noResults = Transformations.map(streamLoadingState)
    { it is State.Success && it.streams.isEmpty() }

    /**
     * If the user clicked a stream, which can be converted into a direct stream link,
     * this is the state of that conversion. Null if no conversion was ever done.
     */
    val directStreamLoadingState: LiveData<DirectStreamLoading?> = _directLoadingState

    /**
     * Whether a direct link conversion is currently being performed.
     */
    val loadingDirectLink: LiveData<Boolean> = Transformations.map(directStreamLoadingState)
    { it != null && it is DirectStreamLoading.Loading }


    /**
     * Search for a TV show or a movie.
     */
    fun fetchStreams(info: StreamableKey) {
        performStatefulOneshotOperation(_state, State.Loading, State.Idle) {
            fetchStreamsToState(info)
        }
    }

    /**
     * Try to get direct link to video.
     */
    fun fetchStreamDirect(ref: VideoStreamRef, download: Boolean) {
        performStatefulOneshotOperation(
            _directLoadingState,
            DirectStreamLoading.Loading(ref, download),
            null
        ) { fetchStreamsToResult(ref, download) }
    }

    private suspend fun fetchStreamsToResult(
        ref: VideoStreamRef,
        download: Boolean
    ): DirectStreamLoading {
        val result = linkExtractor.extractVideoLink(ref.url)
            .getOrElse { return DirectStreamLoading.Failed(ref, it) }
        return DirectStreamLoading.Success(ref, download, result)
    }


    /**
     * Download the video from the URL.
     */
    fun downloadVideo(link: String) {
        val state = streamLoadingState.value as State.Success
        downloadService.downloadFileToFiles(Uri.parse(link), state.streamable)
    }

    private suspend fun fetchStreamsToState(info: StreamableKey): State {
        val streamable = tvDataService.getStreamable(info)
            .getOrElse { return State.Error }
        _streamableName.value = streamableToName(streamable.streamable)
        val result = extractionService.fetchStreams(streamable)
            .getOrElse { return State.Error }
        return State.Success(result.info, result.streams)
    }

    private fun streamableToName(streamable: Streamable): String {
        return when (streamable) {
            is Episode -> (streamable.name ?: streamable.number.toString())
            is TvItem.Movie -> streamable.name
        }
    }

    sealed class State {
        object Idle : State()
        object Loading : State()
        data class Success(
            val streamable: StreamableInfo,
            val streams: List<UnloadedVideoStreamRef>
        ) : State()

        object Error : State()

        val success: Boolean
            get() = this is Success
    }

    sealed class DirectStreamLoading {
        abstract val video: VideoStreamRef

        data class Loading(
            override val video: VideoStreamRef,
            val download: Boolean
        ) : DirectStreamLoading()

        data class Success(
            override val video: VideoStreamRef,
            val download: Boolean,
            val directLink: String
        ) : DirectStreamLoading()

        data class Failed(
            override val video: VideoStreamRef,
            val error: Throwable
        ) : DirectStreamLoading()
    }
}