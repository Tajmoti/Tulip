package com.tajmoti.libtulip.ui.player

import com.tajmoti.commonutils.logger
import com.tajmoti.commonutils.map
import com.tajmoti.libtulip.model.info.StreamableInfo
import com.tajmoti.libtulip.model.info.TulipEpisodeInfo
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.StreamableKey
import com.tajmoti.libtulip.model.stream.StreamableInfoWithLangLinks
import com.tajmoti.libtulip.model.stream.UnloadedVideoStreamRef
import com.tajmoti.libtulip.model.subtitle.SubtitleInfo
import com.tajmoti.libtulip.repository.*
import com.tajmoti.libtulip.service.*
import com.tajmoti.libtulip.ui.doCancelableJob
import com.tajmoti.libtulip.ui.logAllFlowValues
import com.tajmoti.libtulip.ui.streams.FailedLink
import com.tajmoti.libtulip.ui.streams.LoadedLink
import com.tajmoti.libtulip.ui.streams.SelectedLink
import com.tajmoti.libtvprovider.*
import com.tajmoti.libtvvideoextractor.CaptchaInfo
import com.tajmoti.libtvvideoextractor.ExtractionError
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class VideoPlayerViewModelImpl constructor(
    private val subtitleRepository: SubtitleRepository,
    private val playingHistoryRepository: PlayingHistoryRepository,
    private val tmdbTvDataRepository: TmdbTvDataRepository,
    private val hostedTvDataRepository: HostedTvDataRepository,
    private val downloadService: VideoDownloadService,
    private val streamsRepository: StreamsRepository,
    private val streamService: LanguageMappingStreamService,
    private val subDirectory: File,
    private val viewModelScope: CoroutineScope,
    streamableKeyInitial: StreamableKey,
) : VideoPlayerViewModel {
    override val streamableKey = MutableStateFlow(streamableKeyInitial)

    override val isTvShow = streamableKey
        .map(viewModelScope) { it is EpisodeKey }

    /**
     * List of episodes of the currently playing TV show or null if a movie is being played.
     */
    private val episodeList = streamableKey
        .flatMapLatest { getSeasonByKeyAsFlow(it) }
        .map { it?.data?.episodes }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)


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
        .stateIn(viewModelScope, SharingStarted.Eagerly, LinkListLoadingState.Idle)

    /**
     * Manually selected stream to play.
     */
    private val manualStream = MutableSharedFlow<Pair<UnloadedVideoStreamRef, Boolean>?>()

    /**
     * Auto-selected stream to play.
     */
    private val autoStream = streamLoadingStateWithKey
        .mapNotNull { (key, state) -> (state as? LinkListLoadingState.Success)?.let { key to state.streams } }
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
        .map { (it as? LinkListLoadingState.Success)?.streams }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val streamLoadingFinalSuccessState = streamLoadingState
        .map(viewModelScope) { (it as? LinkListLoadingState.Success)?.takeIf { success -> success.final } }

    override val linksAnyResult = streamLoadingState
        .map(viewModelScope) { (it as? LinkListLoadingState.Success)?.streams?.streams?.any() ?: false }

    override val linksNoResult = streamLoadingFinalSuccessState
        .map(viewModelScope) { it?.streams?.streams?.none() ?: false }

    override val linksLoading = streamLoadingState
        .combine(linksNoResult) { state, noResults ->
            state is LinkListLoadingState.Idle
                    || state is LinkListLoadingState.Preparing
                    || state is LinkListLoadingState.Loading
                    || (state is LinkListLoadingState.Success && state.streams.streams.isEmpty() && !noResults)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)


    override val loadingStreamOrDirectLink = linkLoadingState
        .map { it is LinkLoadingState.Loading || it is LinkLoadingState.LoadingDirect }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

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


    /**
     * If a TV show episode is playing, this is the following episode in the same season.
     * Null if the currently playing episode is the last one in the season or a movie is being played.
     */
    private val nextEpisode = combine(episodeList, streamableKey) { a, b -> a to b }
        .map { (episodes, key) -> episodes?.let { selectNextEpisode(it, key) } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    override val hasNextEpisode = nextEpisode
        .map(viewModelScope) { it != null }

    /**
     * State of subtitle list loading.
     */
    private val loadingSubtitlesState = streamableKey
        .flatMapLatest { loadSubtitleList(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SubtitleListLoadingState.Loading)


    /**
     * State of downloading of the selected subtitles.
     */
    private val subtitleDownloadState = MutableStateFlow<SubtitleDownloadingState>(
        SubtitleDownloadingState.Idle
    )

    override val loadingSubtitleList = loadingSubtitlesState
        .map(viewModelScope) { it is SubtitleListLoadingState.Loading }

    override val downloadingSubtitles = subtitleDownloadState
        .map(viewModelScope) { it is SubtitleDownloadingState.Loading }

    override val subtitleList = loadingSubtitlesState
        .map(viewModelScope) { (it as? SubtitleListLoadingState.Success)?.subtitles ?: emptyList() }

    override val subtitlesReadyToSelect = loadingSubtitlesState
        .map(viewModelScope) { it is SubtitleListLoadingState.Success }

    override val subtitleDownloadError = subtitleDownloadState
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
    override val mediaPlayerState = attachedMediaPlayer
        .flatMapLatest { it?.state ?: flowOf(MediaPlayerState.Idle) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, MediaPlayerState.Idle)

    override val isPlaying = mediaPlayerState
        .map(viewModelScope) { it is MediaPlayerState.Playing }

    override val showPlayButton = mediaPlayerState
        .map(viewModelScope, this::playerStateToPlayButtonState)

    override val buffering = mediaPlayerState
        .map(viewModelScope) { state -> (state as? MediaPlayerState.Buffering)?.percent }

    override val position = mediaPlayerState
        .map { state -> state.validPositionOrNull }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /**
     * Progress of the currently playing media or null if nothing is currently playing.
     */
    private val progress = position
        .map { state -> state?.fraction }

    /**
     * Persisted playing position, to be used only to resume playback
     * state once the streamable is initially loaded.
     */
    private val persistedPlayingProgress = streamableKey
        .flatMapLatest {
            playingHistoryRepository
                .getLastPlayedPosition(it)
                .map { position -> it to position?.progress }
        }

    /**
     * Persisted playing progress. This flow will emit a value only once per streamable
     * and a media player. In other words, both the streamable and the media player
     * must change in order for this flow to emit a value.
     *
     * The emitted value is to be directly set to the media player to resume
     * the playing position of a newly loaded streamable.
     */
    private val playerProgressToRestore =
        combine(attachedMediaPlayer.filterNotNull(), streamableKey, persistedPlayingProgress)
        { player, key, keyToProgress -> Triple(player, key, keyToProgress) }
            .distinctUntilChanged { (p1, k1), (p2, k2) -> p1 == p2 && k1 == k2 }
            .filter { (_, key, keyToProgress) -> key == keyToProgress.first }
            .mapNotNull { (player, _, keyPos) -> keyPos.second?.let { player to it } }

    override val isDonePlaying = mediaPlayerState
        .map(viewModelScope) { state -> state is MediaPlayerState.Finished }

    override val isError = mediaPlayerState
        .map(viewModelScope) { state -> state is MediaPlayerState.Error }

    /**
     * State of the interactive subtitle synchronization mechanism.
     * If not null, the user has pressed a button because they
     * saw a word that they want to match to a later shown subtitle text or vice versa.
     */
    private val subSyncState = MutableStateFlow<SubtitleSyncState?>(null)

    override val subtitleOffset = subSyncState
        .map(viewModelScope) { (it as? SubtitleSyncState.OffsetUsed)?.offsetMs ?: 0L }

    /**
     * The current playing position while the media is actively playing,
     * sampled each [PLAY_POSITION_SAMPLE_PERIOD_MS] milliseconds.
     */
    @OptIn(FlowPreview::class)
    private val playingPositionToPersist = combine(streamableKey, progress, isPlaying)
    { key, progress, playing -> Triple(key, progress, playing) }
        // This prevents an existing position from being applied to a newly selected streamable
        .distinctUntilChanged { (_, pos1), (_, pos2) -> pos1 == pos2 }
        .filter { (_, _, playing) -> playing }
        .sample(PLAY_POSITION_SAMPLE_PERIOD_MS)
        .filter { (_, progress) -> progress != null }

    /**
     * Represents fetching of the selected subtitles.
     */
    private var subtitleDownloadJob: Job? = null

    init {
        startPersistPlayingPosition()
        startRestorePlayingPosition()
        logAllFlowValues(
            this,
            viewModelScope,
            logger,
            listOf(
                this::position,
                this::progress,
                this::mediaPlayerState,
                this::persistedPlayingProgress
            )
        )
    }

    override fun goToNextEpisode() {
        nextEpisode.value?.let { streamableKey.value = it }
    }

    override fun changeStreamable(key: StreamableKey) {
        streamableKey.value = key
    }

    override fun onMediaAttached(media: MediaPlayerHelper) {
        attachedMediaPlayer.value = media
    }

    override fun onMediaDetached() {
        attachedMediaPlayer.value = null
    }

    override fun onSubtitlesSelected(subtitleInfo: SubtitleInfo?) {
        subSyncState.value = null
        viewModelScope.doCancelableJob(this::subtitleDownloadJob) {
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

    /**
     * Persists each playing position update to the DB.
     * This allows us to resume playback later.
     */
    private fun startPersistPlayingPosition() {
        viewModelScope.launch {
            playingPositionToPersist.collect { (key, progress) ->
                playingHistoryRepository.setLastPlayedPosition(key, progress)
            }
        }
    }

    /**
     * Loads the time where the playback was last at
     * and restores it to the media player if it is still attached.
     */
    private fun startRestorePlayingPosition() {
        viewModelScope.launch {
            playerProgressToRestore.collect { (player, pos) -> player.progress = pos }
        }
    }

    private fun loadSubtitleList(id: StreamableKey) = flow {
        emit(SubtitleListLoadingState.Loading)
        val subtitles = subtitleRepository.fetchAvailableSubtitles(id)
            .getOrElse { emit(SubtitleListLoadingState.Error); return@flow }
        emit(SubtitleListLoadingState.Success(subtitles))
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


    private fun getSeasonByKeyAsFlow(key: StreamableKey) = when (key) {
        is EpisodeKey.Tmdb -> tmdbTvDataRepository.getSeasonAsFlow(key.seasonKey)
        is EpisodeKey.Hosted -> hostedTvDataRepository.getSeasonAsFlow(key.seasonKey)
        else -> flowOf(null)
    }

    private fun selectNextEpisode(
        episodes: List<TulipEpisodeInfo>,
        key: StreamableKey,
    ): EpisodeKey? {
        val currentEpisodeIndex = episodes
            .indexOfFirst { episode -> episode.key == key }
            .takeIf { it != -1 }
            ?: return null
        if (currentEpisodeIndex == episodes.size - 1)
            return null
        return episodes[currentEpisodeIndex + 1].key
    }

    private fun playerStateToPlayButtonState(state: MediaPlayerState) = when (state) {
        is MediaPlayerState.Buffering -> VideoPlayerViewModel.PlayButtonState.HIDE
        is MediaPlayerState.Error -> VideoPlayerViewModel.PlayButtonState.HIDE
        is MediaPlayerState.Idle -> VideoPlayerViewModel.PlayButtonState.HIDE
        is MediaPlayerState.Paused -> VideoPlayerViewModel.PlayButtonState.SHOW_PLAY
        is MediaPlayerState.Playing -> VideoPlayerViewModel.PlayButtonState.SHOW_PAUSE
        is MediaPlayerState.Finished -> VideoPlayerViewModel.PlayButtonState.HIDE
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

    init {
        logAllFlowValues(this, viewModelScope, logger)
    }

    override fun onStreamClicked(stream: UnloadedVideoStreamRef, download: Boolean) {
        viewModelScope.launch { manualStream.emit(stream to download) }
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
        download: Boolean,
    ) = flow {
        emit(LinkLoadingState.Loading(info, download))
        streamsRepository.resolveStream(info)
            .onSuccess { emitAll(processResolvedLink(ref, it, download)) }
            .onFailure { emit(LinkLoadingState.Error(info, download, null)) }
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
        emit(LinkLoadingState.LoadingDirect(info, download))
        val result = streamsRepository.extractVideoLink(info)
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
        val state = streamLoadingState.value as LinkListLoadingState.Success
        downloadService.downloadFileToFiles(link, state.streams.info)
    }

    private fun fetchStreams(info: StreamableKey) = flow {
        val result = streamService.getStreamsWithLanguages(info)
            .map { result -> result.fold({ LinkListLoadingState.Error(it) }, { LinkListLoadingState.Success(it, false) }) }
            .shareIn(viewModelScope, SharingStarted.Eagerly, 1)
        emitAll(result)
        (result.lastOrNull() as? LinkListLoadingState.Success)
            ?.let { emit(LinkListLoadingState.Success(it.streams, true)) }
    }

    private fun stateToStreamableInfo(it: LinkListLoadingState): StreamableInfo? {
        return when (it) {
            is LinkListLoadingState.Loading -> it.info
            is LinkListLoadingState.Success -> it.streams.info
            is LinkListLoadingState.Error -> it.info
            else -> null
        }
    }


    sealed interface LinkListLoadingState {
        /**
         * Marker state before the loading is started.
         */
        object Idle : LinkListLoadingState

        /**
         * Loading item information from the DB.
         */
        object Preparing : LinkListLoadingState

        /**
         * Loading streams from the TV provider website.
         */
        data class Loading(val info: StreamableInfo) : LinkListLoadingState

        /**
         * Streamable item loaded successfully.
         */
        data class Success(
            val streams: StreamableInfoWithLangLinks,
            /**
             * Whether this is the final value and no more will be loaded.
             */
            val final: Boolean,
        ) : LinkListLoadingState

        /**
         * Error during loading of the item.
         * Null if loading from the DB failed.
         */
        data class Error(val info: StreamableInfo?) : LinkListLoadingState

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
            val download: Boolean,
        ) : LinkLoadingState

        /**
         * Direct link extraction is not supported for the clicked streaming site.
         */
        data class DirectLinkUnsupported(
            val stream: VideoStreamRef.Resolved,
            val download: Boolean,
        ) : LinkLoadingState

        /**
         * A direct video link is being extracted.
         */
        data class LoadingDirect(
            val stream: VideoStreamRef.Resolved,
            val download: Boolean,
        ) : LinkLoadingState

        /**
         * A direct video link was extracted successfully.
         */
        data class LoadedDirect(
            val stream: VideoStreamRef.Resolved,
            val download: Boolean,
            val directLink: String,
        ) : LinkLoadingState

        data class Error(
            val stream: VideoStreamRef,
            val download: Boolean,
            val captcha: CaptchaInfo?,
        ) : LinkLoadingState
    }
}