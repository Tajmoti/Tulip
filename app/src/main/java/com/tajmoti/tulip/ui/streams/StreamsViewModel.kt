package com.tajmoti.tulip.ui.streams

import androidx.lifecycle.*
import com.tajmoti.libtvprovider.TvProvider
import com.tajmoti.libtvprovider.stream.VideoStreamRef
import com.tajmoti.libtvvideoextractor.LinkExtractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.io.Serializable
import javax.inject.Inject

@HiltViewModel
class StreamsViewModel @Inject constructor(
    private val tvProvider: TvProvider,
    private val linkExtractor: LinkExtractor
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
    fun fetchStreams(streamableId: Serializable) {
        if (_state.value is State.Loading)
            return
        _state.value = State.Loading
        viewModelScope.launch {
            fetchStreamsAsync(streamableId)
        }
    }

    /**
     * Try to get direct link to video.
     */
    fun fetchStreamDirect(ref: VideoStreamRef) {
        val value = directStreamLoadingState.value
        if (value != null && value is DirectStreamLoading.Loading)
            return
        _directLoadingState.value = DirectStreamLoading.Loading(ref)
        viewModelScope.launch {
            val link = linkExtractor.tryExtractLink(ref.url)
            link.onSuccess { _directLoadingState.value = DirectStreamLoading.Success(ref, it) }
            link.onFailure { _directLoadingState.value = DirectStreamLoading.Failed(ref, it) }
        }
    }

    private suspend fun fetchStreamsAsync(streamableId: Serializable) {
        try {
            val streamableResult = tvProvider.getStreamable(streamableId)
            streamableResult.onFailure {
                _state.value = State.Error(it.message ?: it.javaClass.name)
                return
            }
            val streamable = streamableResult.getOrThrow()
            _streamableName.value = streamable.name
            val result = streamable.loadSources()
            result.onSuccess { _state.value = State.Success(mapAndSortLinksByRelevance(it)) }
            result.onFailure { _state.value = State.Error(it.message ?: it.javaClass.name) }
        } catch (e: CancellationException) {
            _state.value = State.Idle
        }
    }

    private fun mapAndSortLinksByRelevance(it: List<VideoStreamRef>): List<UnloadedVideoStreamRef> {
        val mapped = it.map { UnloadedVideoStreamRef(it, linkExtractor.canExtractLink(it.url)) }
        val working = mapped.filter { it.info.working }
        val broken = mapped.filterNot { it.info.working }
        val extractable = working.filter { it.linkExtractionSupported }
        val notExtractable = working.filterNot { it.linkExtractionSupported }
        val badExtractable = broken.filter { it.linkExtractionSupported }
        val badNotExtractor = broken.filterNot { it.linkExtractionSupported }
        return extractable + notExtractable + badExtractable + badNotExtractor
    }

    sealed class State {
        object Idle : State()
        object Loading : State()
        data class Success(val streams: List<UnloadedVideoStreamRef>) : State()
        data class Error(val message: String) : State()

        val success: Boolean
            get() = this is Success
    }

    sealed class DirectStreamLoading {
        abstract val video: VideoStreamRef

        data class Loading(
            override val video: VideoStreamRef
        ) : DirectStreamLoading()

        data class Success(
            override val video: VideoStreamRef,
            val directLink: String
        ) : DirectStreamLoading()

        data class Failed(
            override val video: VideoStreamRef,
            val error: Throwable
        ) : DirectStreamLoading()
    }
}