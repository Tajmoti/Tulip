package com.tajmoti.tulip.ui.streams

import androidx.lifecycle.*
import com.tajmoti.commonutils.mapToAsyncJobs
import com.tajmoti.libtulip.model.info.StreamableInfo
import com.tajmoti.libtulip.model.key.StreamableKey
import com.tajmoti.libtulip.model.stream.FinalizedStreamableInformation
import com.tajmoti.libtulip.service.*
import com.tajmoti.libtvprovider.*
import com.tajmoti.tulip.ui.performStatefulOneshotOperation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject

@HiltViewModel
class StreamsViewModel @Inject constructor(
    private val downloadService: VideoDownloadService,
    private val extractionService: StreamExtractorService,
    private val streamService: LanguageMappingStreamService
) : ViewModel() {
    private val _state = MutableLiveData<State>(State.Idle)

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
            is State.Success -> it.info.info
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
    { it is State.Success && it.info.streams.isEmpty() }


    /**
     * Search for a TV show or a movie.
     */
    fun fetchStreamsWithLanguages(info: StreamableKey) {
        performStatefulOneshotOperation(_state, State.Preparing, State.Idle) {
            fetchStreamsToState(info)
        }
    }

    fun downloadVideo(link: String) {
        val state = streamLoadingState.value as State.Success
        downloadService.downloadFileToFiles(link, state.info.info)
    }

    private suspend fun fetchStreamsToState(info: StreamableKey): State {
        val consumer: (StreamableInfo) -> Unit = { _state.value = State.Loading(it) }
        val shit = streamService.getStreamsWithLanguages(info, consumer)
            .getOrElse { return State.Error((_state.value as? State.Loading)?.info) }
        val information = mapToAsyncJobs(shit.streams) { video ->
            extractionService.finalizeVideoInformation(video)
        }.filterNotNull()
        val finalized = FinalizedStreamableInformation(shit.info, information)
        return State.Success(finalized)
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
        data class Success(val info: FinalizedStreamableInformation) : State()

        /**
         * Error during loading of the item.
         * Null if loading from the DB failed.
         */
        data class Error(val info: StreamableInfo?) : State()

        val success: Boolean
            get() = this is Success
    }
}