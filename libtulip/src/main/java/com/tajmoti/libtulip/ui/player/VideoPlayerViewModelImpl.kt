package com.tajmoti.libtulip.ui.player

import com.tajmoti.commonutils.map
import com.tajmoti.libtulip.misc.NetworkResult
import com.tajmoti.libtulip.model.info.TulipSeasonInfo
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.StreamableKey
import com.tajmoti.libtulip.model.subtitle.SubtitleInfo
import com.tajmoti.libtulip.repository.HostedTvDataRepository
import com.tajmoti.libtulip.repository.PlayingHistoryRepository
import com.tajmoti.libtulip.repository.SubtitleRepository
import com.tajmoti.libtulip.repository.TmdbTvDataRepository
import com.tajmoti.libtulip.service.*
import com.tajmoti.libtulip.ui.doCancelableJob
import com.tajmoti.libtvprovider.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class VideoPlayerViewModelImpl constructor(
    private val subtitleRepository: SubtitleRepository,
    private val playingHistoryRepository: PlayingHistoryRepository,
    private val tmdbTvDataRepository: TmdbTvDataRepository,
    private val hostedTvDataRepository: HostedTvDataRepository,
    private val subDirectory: File,
    private val viewModelScope: CoroutineScope,
    streamableKeyInitial: StreamableKey
) : VideoPlayerViewModel {
    override val streamableKey = MutableStateFlow(streamableKeyInitial)

    override val isTvShow = streamableKey
        .map(viewModelScope) { it is EpisodeKey }

    override val episodeList = streamableKey
        .flatMapLatest { getSeasonFlow(it) }
        .map { it?.data?.episodes }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private fun getSeasonFlow(
        streamableKey: StreamableKey
    ): Flow<NetworkResult<out TulipSeasonInfo>?> {
        return when (streamableKey) {
            is EpisodeKey.Tmdb -> tmdbTvDataRepository.getSeasonAsFlow(streamableKey.seasonKey)
            is EpisodeKey.Hosted -> hostedTvDataRepository.getSeasonAsFlow(streamableKey.seasonKey)
            else -> flowOf(null)
        }
    }

    override val nextEpisode = combine(episodeList, streamableKey) { a, b -> a to b }
        .map { (episodes, streamableKey) ->
            val currentEpisodeIndex = episodes
                ?.indexOfFirst { episode -> episode.key == streamableKey }
                ?: return@map null
            if (currentEpisodeIndex == episodes.size - 1)
                return@map null
            episodes[currentEpisodeIndex + 1].key
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /**
     * State of subtitle list loading
     */
    private val loadingSubtitlesState = streamableKey
        .flatMapLatest { loadSubtitlesList(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SubtitleListLoadingState.Loading)


    /**
     * State of downloading of selected subtitles
     */
    private val subtitleDownloadState = MutableStateFlow<SubtitleDownloadingState>(
        SubtitleDownloadingState.Idle
    )

    override val loadingSubtitles = MutableStateFlow(false)

    override val downloadingSubtitleFile = MutableStateFlow(false)

    override val subtitleList = loadingSubtitlesState
        .map(viewModelScope) { (it as? SubtitleListLoadingState.Success)?.subtitles ?: emptyList() }

    override val subtitlesReadyToSelect = loadingSubtitlesState
        .map(viewModelScope) { it is SubtitleListLoadingState.Success }

    override val downloadingError = subtitleDownloadState
        .map(viewModelScope) { it is SubtitleDownloadingState.Error }

    override val subtitleFile = subtitleDownloadState
        .map(viewModelScope) { (it as? SubtitleDownloadingState.Success)?.subtitles }

    /**
     * Currently attached media player.
     */
    private val attachedMediaPlayer = MutableStateFlow<MediaPlayerHelper?>(null)

    /**
     * State of the currently attached media player.
     */
    private val mediaPlayerState = attachedMediaPlayer
        .flatMapLatest { it?.state ?: flowOf(MediaPlayerHelper.State.Idle) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, MediaPlayerHelper.State.Idle)

    override val isPlaying = mediaPlayerState.map(viewModelScope) {
        it is MediaPlayerHelper.State.Playing
    }

    override val showPlayButton = mediaPlayerState
        .map(viewModelScope) {
            when (it) {
                is MediaPlayerHelper.State.Buffering -> VideoPlayerViewModel.PlayButtonState.HIDE
                is MediaPlayerHelper.State.Error -> VideoPlayerViewModel.PlayButtonState.HIDE
                is MediaPlayerHelper.State.Idle -> VideoPlayerViewModel.PlayButtonState.HIDE
                is MediaPlayerHelper.State.Paused -> VideoPlayerViewModel.PlayButtonState.SHOW_PLAY
                is MediaPlayerHelper.State.Playing -> VideoPlayerViewModel.PlayButtonState.SHOW_PAUSE
                is MediaPlayerHelper.State.Finished -> VideoPlayerViewModel.PlayButtonState.SHOW_PLAY
            }
        }

    override val buffering = mediaPlayerState
        .map(viewModelScope) { state -> (state as? MediaPlayerHelper.State.Buffering)?.percent }

    override val position = mediaPlayerState
        .map { state ->
            when (state) {
                is MediaPlayerHelper.State.Buffering -> state.position
                is MediaPlayerHelper.State.Error -> null
                is MediaPlayerHelper.State.Idle -> null
                is MediaPlayerHelper.State.Paused -> state.position
                is MediaPlayerHelper.State.Playing -> state.position
                is MediaPlayerHelper.State.Finished -> null
            }
        }
        .filter { it == null || isValidPosition(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /**
     * Playing progress (as a real number from 0.0 to 1.0) or null if nothing is currently playing.
     */
    val progress = mediaPlayerState
        .map { state ->
            when (state) {
                is MediaPlayerHelper.State.Buffering -> state.position
                is MediaPlayerHelper.State.Error -> null
                is MediaPlayerHelper.State.Idle -> null
                is MediaPlayerHelper.State.Paused -> state.position
                is MediaPlayerHelper.State.Playing -> state.position
                is MediaPlayerHelper.State.Finished -> null
            }
        }
        .filter { it?.let { isValidPosition(it) } ?: true }
        .map { it?.fraction }

    private fun isValidPosition(it: Position): Boolean {
        return (it.timeMs > 0 && it.fraction > 0.0f)
    }

    /**
     * Persisted playing position, should be used only to resume playback state
     * the first time the media is loaded.
     */
    private val persistedPlayingProgress = streamableKey
        .flatMapLatest {
            playingHistoryRepository
                .getLastPlayedPosition(it)
                .map { position -> it to position?.progress }
        }

    override val isDonePlaying = mediaPlayerState
        .map(viewModelScope) { state -> state is MediaPlayerHelper.State.Finished }

    override val isError = mediaPlayerState
        .map(viewModelScope) { state -> state is MediaPlayerHelper.State.Error }

    /**
     * State of the interactive subtitle synchronization mechanism.
     * If not null, the user has pressed a button because they
     * saw a word that they want to match to a later shown subtitle text or vice versa.
     */
    private val subSyncState = MutableStateFlow<SubtitleSyncState?>(null)

    override val subtitleOffset = subSyncState
        .map(viewModelScope) { (it as? SubtitleSyncState.OffsetUsed)?.offsetMs ?: 0L }

    /**
     * Represents fetching of the selected subtitles.
     */
    private var subtitleDownloadJob: Job? = null

    init {
        persistPlayingPosition()
        viewModelScope.launch { restorePlayerProgress() }
    }

    /**
     * Persists each playing position update to the DB.
     * This allows us to resume playback later.
     */
    @OptIn(FlowPreview::class)
    private fun persistPlayingPosition() {
        viewModelScope.launch {
            progress.sample(PLAY_POSITION_SAMPLE_PERIOD_MS)
                .filterNotNull()
                .collect {
                    playingHistoryRepository.setLastPlayedPosition(streamableKey.value, it)
                }
        }
    }

    private fun loadSubtitlesList(id: StreamableKey) = flow {
        emit(SubtitleListLoadingState.Loading)
        val subtitles = subtitleRepository.fetchAvailableSubtitles(id)
            .getOrElse { emit(SubtitleListLoadingState.Error); return@flow }
        emit(SubtitleListLoadingState.Success(subtitles))
    }

    override fun goToNextEpisode() {
        nextEpisode.value?.let { streamableKey.value = it }
    }

    override fun onMediaAttached(media: MediaPlayerHelper) {
        attachedMediaPlayer.value = media
    }

    /**
     * Loads the time where the playback was last at
     * and restores it to the media player if it is still attached.
     */
    private suspend fun restorePlayerProgress() {
        val player = attachedMediaPlayer.filterNotNull()
        val key = streamableKey
        val progress = persistedPlayingProgress
        combine(player, key, progress) { a, b, c -> Triple(a, b, c) }
            .distinctUntilChanged { a, b -> a.first == b.first && a.second == b.second }
            .filter { (_, key, keyToProgress) -> key == keyToProgress.first }
            .mapNotNull { (player, _, keyPos) -> keyPos.second?.let { player to it } }
            .collect { (player, pos) -> player.progress = pos }
    }

    override fun onMediaDetached() {
        attachedMediaPlayer.value = null
    }

    override fun onSubtitlesSelected(subtitleInfo: SubtitleInfo?) {
        subSyncState.value = null
        viewModelScope.doCancelableJob(this::subtitleDownloadJob, downloadingSubtitleFile) {
            if (subtitleInfo != null) {
                subtitleDownloadState.emitAll(downloadSubtitles(subtitleInfo))
            } else {
                subtitleDownloadState.emit(SubtitleDownloadingState.Idle)
            }
        }
    }

    override fun onWordHeard(time: Long) {
        val seen = subSyncState.value as? SubtitleSyncState.Seen
        if (seen == null) {
            val existingOffset = subSyncState.value?.offsetMs ?: 0L
            subSyncState.value = SubtitleSyncState.Heard(time, existingOffset)
        } else {
            calculateAndSetSubtitleDelay(time, seen.time)
        }
    }

    override fun onTextSeen(time: Long) {
        val heard = subSyncState.value as? SubtitleSyncState.Heard
        if (heard == null) {
            val existingOffset = subSyncState.value?.offsetMs ?: 0L
            subSyncState.value = SubtitleSyncState.Seen(time, existingOffset)
        } else {
            calculateAndSetSubtitleDelay(heard.time, time)
        }
    }

    private fun calculateAndSetSubtitleDelay(heardTime: Long, seenTime: Long) {
        val existingOffset = subSyncState.value?.offsetMs ?: 0L
        val newOffset = heardTime - seenTime
        val delaySubtitlesByMs = existingOffset + newOffset
        subSyncState.value = SubtitleSyncState.OffsetUsed(delaySubtitlesByMs)
    }

    private fun downloadSubtitles(subtitleInfo: SubtitleInfo) = flow {
        emit(SubtitleDownloadingState.Loading)
        val subtitleStream = subtitleRepository.downloadSubtitleToFile(subtitleInfo, subDirectory)
            .getOrElse { emit(SubtitleDownloadingState.Error); return@flow }
        emit(SubtitleDownloadingState.Success(subtitleStream))
    }

    sealed interface SubtitleListLoadingState {
        object Loading : SubtitleListLoadingState
        data class Success(val subtitles: List<SubtitleInfo>) : SubtitleListLoadingState
        object Error : SubtitleListLoadingState
    }

    sealed interface SubtitleDownloadingState {
        object Idle : SubtitleDownloadingState
        object Loading : SubtitleDownloadingState
        data class Success(val subtitles: File) : SubtitleDownloadingState
        object Error : SubtitleDownloadingState
    }

    sealed interface SubtitleSyncState {
        /**
         * Subtitle offset to use.
         * Positive values mean that subtitles must be delayed,
         * negative values mean they need to be shown earlier.
         */
        val offsetMs: Long

        class Heard(val time: Long, override val offsetMs: Long) : SubtitleSyncState
        class Seen(val time: Long, override val offsetMs: Long) : SubtitleSyncState
        class OffsetUsed(override val offsetMs: Long) : SubtitleSyncState
    }

    companion object {
        /**
         * Playing position will be stored this often.
         */
        private const val PLAY_POSITION_SAMPLE_PERIOD_MS = 5000L
    }
}