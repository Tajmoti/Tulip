package com.tajmoti.tulip.ui.streams

import android.net.Uri
import androidx.lifecycle.*
import com.tajmoti.libtvprovider.*
import com.tajmoti.libtvvideoextractor.VideoLinkExtractor
import com.tajmoti.tulip.db.AppDatabase
import com.tajmoti.tulip.model.StreamableIdentifier
import com.tajmoti.tulip.model.StreamableInfo
import com.tajmoti.tulip.model.StreamingService
import com.tajmoti.tulip.service.StreamExtractorService
import com.tajmoti.tulip.service.VideoDownloadService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StreamsViewModel @Inject constructor(
    private val tvProvider: MultiTvProvider<StreamingService>,
    private val linkExtractor: VideoLinkExtractor,
    private val db: AppDatabase,
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
    fun fetchStreams(info: StreamableIdentifier) {
        if (_state.value is State.Loading)
            return
        _state.value = State.Loading
        viewModelScope.launch {
            fetchStreamsAsync(info)
        }
    }

    /**
     * Try to get direct link to video.
     */
    fun fetchStreamDirect(ref: VideoStreamRef, download: Boolean) {
        val value = directStreamLoadingState.value
        if (value != null && value is DirectStreamLoading.Loading)
            return
        _directLoadingState.value = DirectStreamLoading.Loading(ref, download)
        viewModelScope.launch {
            val result = linkExtractor.extractVideoLink(ref.url)
            result.onFailure {
                _directLoadingState.value = DirectStreamLoading.Failed(ref, it)
            }
            result.onSuccess {
                _directLoadingState.value = DirectStreamLoading.Success(ref, download, it)
            }
        }
    }


    /**
     * Download the video from the URL.
     */
    fun downloadVideo(link: String) {
        val state = streamLoadingState.value as State.Success
        downloadService.downloadFileToFiles(Uri.parse(link), state.streamable)
    }

    private suspend fun fetchStreamsAsync(info: StreamableIdentifier) {
        try {
            val streamable = getStreamableByInfo(info).getOrElse {
                _state.value = State.Error(it.message ?: it.javaClass.name)
                return
            }
            _streamableName.value = streamableToName(streamable.streamable)
            val result = extractionService.fetchStreams(streamable)
            result.onFailure { _state.value = State.Error(it.message ?: it.javaClass.name) }
            result.onSuccess {
                _state.value = State.Success(it.info, it.streams)
            }
        } catch (e: CancellationException) {
            _state.value = State.Idle
        }
    }

    private suspend fun getStreamableByInfo(item: StreamableIdentifier): Result<StreamableInfo> {
        val service = item.service
        return when (item) {
            is StreamableIdentifier.TvShow -> {
                val dbShow = db.tvShowDao().getByKey(service, item.tvShow)
                    ?: TODO()
                val dbSeason = db.seasonDao().getForShow(service, item.tvShow, item.season)
                    ?: TODO()
                val dbEpisode = db.episodeDao()
                    .getByKey(service, item.tvShow, item.season, item.key)
                    ?: TODO()
                tvProvider.getEpisode(service, dbEpisode.apiInfo)
                    .map { StreamableInfo.TvShow(dbShow, dbSeason, it) }
            }
            is StreamableIdentifier.Movie -> {
                val dbMovie = db.movieDao()
                    .getByKey(service, item.key) ?: TODO()
                tvProvider.getMovie(service, dbMovie.apiInfo)
                    .map { StreamableInfo.Movie(it) }
            }
        }
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

        data class Error(val message: String) : State()

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