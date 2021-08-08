package com.tajmoti.tulip.ui.streams

import androidx.lifecycle.*
import com.tajmoti.libtvprovider.*
import com.tajmoti.libtvvideoextractor.VideoLinkExtractor
import com.tajmoti.tulip.db.AppDatabase
import com.tajmoti.tulip.model.DbSeason
import com.tajmoti.tulip.model.DbTvShow
import com.tajmoti.tulip.model.StreamingService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StreamsViewModel @Inject constructor(
    private val tvProvider: MultiTvProvider<StreamingService>,
    private val linkExtractor: VideoLinkExtractor,
    private val db: AppDatabase
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
    fun fetchStreams(service: StreamingService, info: StreamInfo) {
        if (_state.value is State.Loading)
            return
        _state.value = State.Loading
        viewModelScope.launch {
            fetchStreamsAsync(service, info)
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
            val link = linkExtractor.extractVideoLink(ref.url)
            link.onSuccess {
                _directLoadingState.value = DirectStreamLoading.Success(ref, download, it)
            }
            link.onFailure { _directLoadingState.value = DirectStreamLoading.Failed(ref, it) }
        }
    }

    private suspend fun fetchStreamsAsync(service: StreamingService, item: StreamInfo) {
        try {
            val (streamable, name) = getStreamableAndNameByInfo(service, item).getOrElse {
                _state.value = State.Error(it.message ?: it.javaClass.name)
                return
            }
            _streamableName.value = name
            val result = streamable.streamable.loadSources().getOrElse {
                _state.value = State.Error(it.message ?: it.javaClass.name)
                return
            }
            _state.value = State.Success(streamable, mapAndSortLinksByRelevance(result))
        } catch (e: CancellationException) {
            _state.value = State.Idle
        }
    }

    private suspend fun getStreamableAndNameByInfo(
        service: StreamingService,
        item: StreamInfo
    ): Result<Pair<FullStreamableInfo, String>> {
        return when (item) {
            is StreamInfo.TvShow -> {
                val dbShow = db.tvShowDao().getByKey(service, item.tvShow)
                    ?: TODO()
                val dbSeason = db.seasonDao().getForShow(service, item.tvShow, item.season)
                    ?: TODO()
                val dbEpisode = db.episodeDao()
                    .getByKey(service, item.tvShow, item.season, item.key)
                    ?: TODO()
                tvProvider.getEpisode(service, dbEpisode.apiInfo)
                    .map {
                        val fullInfo = FullStreamableInfo.TvShow(dbShow, dbSeason, it)
                        fullInfo to (it.name ?: it.number.toString())
                    }
            }
            is StreamInfo.Movie -> {
                val dbMovie = db.movieDao()
                    .getByKey(service, item.key) ?: TODO()
                tvProvider.getMovie(service, dbMovie.apiInfo)
                    .map { FullStreamableInfo.Movie(it) to it.name }
            }
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
        data class Success(
            val streamable: FullStreamableInfo,
            val streams: List<UnloadedVideoStreamRef>
        ) : State()

        data class Error(val message: String) : State()

        val success: Boolean
            get() = this is Success
    }

    sealed class FullStreamableInfo(
        val streamable: Streamable
    ) {

        data class TvShow(
            val show: DbTvShow,
            val season: DbSeason,
            val episode: Episode
        ) : FullStreamableInfo(episode)

        data class Movie(
            val movie: TvItem.Movie
        ) : FullStreamableInfo(movie)
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

    sealed class StreamInfo {
        class Movie(val key: String) : StreamInfo()
        class TvShow(val tvShow: String, val season: String, val key: String) : StreamInfo()
    }
}