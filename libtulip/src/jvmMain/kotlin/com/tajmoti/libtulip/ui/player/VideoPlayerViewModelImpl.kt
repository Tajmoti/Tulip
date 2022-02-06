package com.tajmoti.libtulip.ui.player

import com.tajmoti.commonutils.logger
import com.tajmoti.commonutils.map
import com.tajmoti.libtulip.model.info.LanguageCode
import com.tajmoti.libtulip.model.info.StreamableInfo
import com.tajmoti.libtulip.model.info.TulipEpisodeInfo
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.StreamableKey
import com.tajmoti.libtulip.model.stream.StreamsResult
import com.tajmoti.libtulip.model.stream.UnloadedVideoStreamRef
import com.tajmoti.libtulip.model.subtitle.SubtitleInfo
import com.tajmoti.libtulip.repository.*
import com.tajmoti.libtulip.service.StreamExtractionService
import com.tajmoti.libtulip.service.StreamService
import com.tajmoti.libtulip.service.SubtitleService
import com.tajmoti.libtulip.service.VideoDownloadService
import com.tajmoti.libtulip.ui.logAllFlowValues
import com.tajmoti.libtulip.ui.streams.FailedLink
import com.tajmoti.libtulip.ui.streams.LoadedLink
import com.tajmoti.libtulip.ui.streams.SelectedLink
import com.tajmoti.libtvprovider.model.VideoStreamRef
import com.tajmoti.libtvvideoextractor.CaptchaInfo
import com.tajmoti.libtvvideoextractor.ExtractionError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class VideoPlayerViewModelImpl constructor(
    private val subtitleRepository: SubtitleRepository,
    private val playingHistoryRepository: PlayingHistoryRepository,
    private val tmdbTvDataRepository: TmdbTvDataRepository,
    private val hostedTvDataRepository: HostedTvDataRepository,
    private val downloadService: VideoDownloadService,
    private val streamExtractionService: StreamExtractionService,
    private val streamService: StreamService,
    private val subtitleService: SubtitleService,
    private val subDirectory: String,
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
        .flatMapLatest { getSeasonByKey(it) }
        .map { it?.data?.episodes }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)


    /**
     * Streamable key to loading state of the list of available streams.
     */
    private val keyWithLinkListLoadingState = streamableKey
        .flatMapLatest { key ->
            streamService.getStreamsByKey(key)
                .map(::mapStreamsResult)
                .map { key to it }
        }
        .shareIn(viewModelScope, SharingStarted.Eagerly)

    /**
     * Loading state of the list of available streams.
     */
    private val linkListLoadingState = keyWithLinkListLoadingState
        .map { (_, state) -> state }
        .stateIn(viewModelScope, SharingStarted.Eagerly, LinkListLoadingState.Loading)

    /**
     * Manually selected stream to play.
     */
    private val manualStream = MutableSharedFlow<Pair<UnloadedVideoStreamRef, Boolean>?>()

    /**
     * Auto-selected stream to play.
     */
    private val autoStream = keyWithLinkListLoadingState
        .mapNotNull { (key, state) -> (state as? LinkListLoadingState.Success)?.let { key to state.streams } }
        .filter { (_, streams) -> anyGoodStreams(streams) }
        .distinctUntilChangedBy { (key, _) -> key }
        .map { (_, streams) -> firstGoodStream(streams) to false }
        .shareIn(viewModelScope, SharingStarted.Lazily)

    private fun firstGoodStream(it: List<UnloadedVideoStreamRef>) =
        it.first { video -> video.linkExtractionSupported }

    private fun anyGoodStreams(it: List<UnloadedVideoStreamRef>) =
        it.any { s -> s.linkExtractionSupported }

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

    override val streamableInfo = streamableKey
        .flatMapLatest { getStreamableInfo(it) }
        .map { it.getOrNull() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)


    override val linksResult = linkListLoadingState
        .map(viewModelScope) { (it as? LinkListLoadingState.Success)?.streams }

    private val streamLoadingFinalSuccessState = linkListLoadingState
        .map(viewModelScope) { (it as? LinkListLoadingState.Success)?.takeIf { success -> success.final } }

    override val linksAnyResult = linkListLoadingState
        .map(viewModelScope) {
            (it as? LinkListLoadingState.Success)?.streams?.any() ?: false
        }

    override val linksNoResult = streamLoadingFinalSuccessState
        .map(viewModelScope) { it?.streams?.none() ?: false }

    override val linksLoading = linkListLoadingState
        .combine(linksNoResult) { state, noResults ->
            state is LinkListLoadingState.Loading
                    || (state is LinkListLoadingState.Success && state.streams.isEmpty() && !noResults)
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
                ?.let { FailedLink(it.stream, it.languageCode, it.download, it.captcha) }
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
     * Subtitles that should be downloaded and applied.
     */
    private val subtitlesToDownload = MutableStateFlow<SubtitleInfo?>(null)

    /**
     * State of downloading of the selected subtitles.
     */
    private val subtitleDownloadState = subtitlesToDownload
        .flatMapLatest { if (it != null) downloadSubtitles(it) else flowOf(SubtitleDownloadingState.Idle) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SubtitleDownloadingState.Idle)

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
    private val attachedMediaPlayer = MutableStateFlow<VideoPlayer?>(null)

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
        .map(viewModelScope) { state -> state.validPositionOrNull }

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
    private val playingPositionToPersist = combine(streamableKey, progress, isPlaying)
    { key, progress, playing -> Triple(key, progress, playing) }
        // This prevents an existing position from being applied to a newly selected streamable
        .distinctUntilChanged { (_, pos1), (_, pos2) -> pos1 == pos2 }
        .filter { (_, _, playing) -> playing }
        .sample(PLAY_POSITION_SAMPLE_PERIOD_MS)
        .mapNotNull { (key, progress) -> progress?.let { key to progress } }

    init {
        startPersistPlayingPosition()
        startRestorePlayingPosition()
        if (logger.isDebugEnabled) {
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
    }

    override fun goToNextEpisode() {
        nextEpisode.value?.let { changeStreamable(it) }
    }

    override fun changeStreamable(key: StreamableKey) {
        // This prevents the video from stopping if the currently playing streamable is selected
        if (streamableKey.value == key)
            return
        viewModelScope.launch {
            manualStream.emit(null)
            streamableKey.emit(key)
        }
    }

    override fun onMediaAttached(media: VideoPlayer) {
        attachedMediaPlayer.value = media
    }

    override fun onMediaDetached() {
        attachedMediaPlayer.value = null
    }

    override fun onSubtitlesSelected(subtitleInfo: SubtitleInfo?) {
        subSyncState.value = null
        subtitlesToDownload.value = subtitleInfo
    }

    override fun onWordHeard() {
        val time = position.value?.timeMs ?: return
        val seen = subSyncState.value as? SubtitleSyncState.Seen
        if (seen == null) {
            val existingOffset = subSyncState.value?.offsetMs ?: 0L
            subSyncState.value = SubtitleSyncState.Heard(time, existingOffset)
        } else {
            calculateAndSetSubtitleDelay(time, seen.time)
        }
    }

    override fun onTextSeen() {
        val time = position.value?.timeMs ?: return
        val heard = subSyncState.value as? SubtitleSyncState.Heard
        if (heard == null) {
            val existingOffset = subSyncState.value?.offsetMs ?: 0L
            subSyncState.value = SubtitleSyncState.Seen(time, existingOffset)
        } else {
            calculateAndSetSubtitleDelay(heard.time, time)
        }
    }

    override fun skipForwards() {
        // TODO Not the best solution, probably merge with playerProgressToRestore
        attachedMediaPlayer.value?.let {
            it.time = (it.time + REWIND_TIME_MS).coerceAtMost(it.length)
        }
    }

    override fun skipBackwards() {
        // TODO Not the best solution, probably merge with playerProgressToRestore
        attachedMediaPlayer.value?.let { it.time = (it.time - REWIND_TIME_MS).coerceAtLeast(0) }
    }

    override fun playPause() {
        // TODO Not the best solution
        attachedMediaPlayer.value?.playOrPause()
    }

    override fun setPlaybackProgress(progress: Float) {
        // TODO Not the best solution
        attachedMediaPlayer.value?.position = progress
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
     * Applies [playerProgressToRestore] to the media player to restore playback progress.
     */
    private fun startRestorePlayingPosition() {
        viewModelScope.launch {
            playerProgressToRestore.collect { (player, pos) -> player.position = pos }
        }
    }

    private fun loadSubtitleList(id: StreamableKey) = flow {
        emit(SubtitleListLoadingState.Loading)
        val result = subtitleRepository.getAvailableSubtitles(id)
            .fold({ SubtitleListLoadingState.Success(it) }, { SubtitleListLoadingState.Error })
        emit(result)
    }

    private fun calculateAndSetSubtitleDelay(heardTime: Long, seenTime: Long) {
        val existingOffset = subSyncState.value?.offsetMs ?: 0L
        val newOffset = heardTime - seenTime
        val delaySubtitlesByMs = existingOffset + newOffset
        subSyncState.value = SubtitleSyncState.OffsetUsed(delaySubtitlesByMs)
    }

    private fun downloadSubtitles(subtitleInfo: SubtitleInfo) = flow {
        emit(SubtitleDownloadingState.Loading)
        val subtitleResult = subtitleService.downloadSubtitleToFile(subtitleInfo, subDirectory)
            .fold({ SubtitleDownloadingState.Success(it) }, { SubtitleDownloadingState.Error })
        emit(subtitleResult)
    }


    private fun getSeasonByKey(key: StreamableKey) = when (key) {
        is EpisodeKey.Tmdb -> tmdbTvDataRepository.getSeason(key.seasonKey)
        is EpisodeKey.Hosted -> hostedTvDataRepository.getSeason(key.seasonKey)
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
        streamExtractionService.resolveStream(info)
            .onSuccess { emitAll(processResolvedLink(ref, it, download)) }
            .onFailure { emit(LinkLoadingState.Error(info, ref.language, download, null)) }
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
        val result = streamExtractionService.extractVideoLink(info)
            .map { result -> if (download) downloadVideo(result); result }
            .fold(
                { LinkLoadingState.Error(info, ref.language, download, captchaOrNull(it)) },
                { LinkLoadingState.LoadedDirect(info, download, it) },
            )
        emit(result)
    }

    private fun captchaOrNull(it: ExtractionError) =
        (it as? ExtractionError.Captcha)?.info

    private fun downloadVideo(link: String) {
        downloadService.downloadFileToFiles(link, streamableInfo.value!!)
    }

    private fun mapStreamsResult(result: StreamsResult): LinkListLoadingState {
        return when (result) {
            is StreamsResult.Success -> LinkListLoadingState.Success(result.streams, result.possiblyFinished)
            is StreamsResult.Error -> LinkListLoadingState.Error
        }
    }

    private fun getStreamableInfo(key: StreamableKey): Flow<Result<StreamableInfo>> {
        return when (key) {
            is EpisodeKey.Tmdb -> tmdbTvDataRepository.getFullEpisodeData(key)
            is MovieKey.Tmdb -> tmdbTvDataRepository.getMovie(key).map { it.toResult() }
            is EpisodeKey.Hosted -> hostedTvDataRepository.getEpisodeInfo(key)
            is MovieKey.Hosted -> hostedTvDataRepository.getMovie(key).map { it.toResult() }
        }
    }

    sealed interface SubtitleListLoadingState {
        object Loading : SubtitleListLoadingState
        data class Success(val subtitles: List<SubtitleInfo>) : SubtitleListLoadingState
        object Error : SubtitleListLoadingState
    }

    sealed interface SubtitleDownloadingState {
        object Idle : SubtitleDownloadingState
        object Loading : SubtitleDownloadingState
        data class Success(val subtitles: String) : SubtitleDownloadingState
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

    sealed interface LinkListLoadingState {
        /**
         * Marker state before the loading is started.
         */
        object Loading : LinkListLoadingState

        /**
         * Streamable item loaded successfully.
         */
        data class Success(
            val streams: List<UnloadedVideoStreamRef>,
            /**
             * Whether this is the final value and no more will be loaded.
             */
            val final: Boolean,
        ) : LinkListLoadingState

        /**
         * Error during loading of the item.
         */
        object Error : LinkListLoadingState
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
            val languageCode: LanguageCode,
            val download: Boolean,
            val captcha: CaptchaInfo?,
        ) : LinkLoadingState
    }

    companion object {
        /**
         * Playing position will be stored this often.
         */
        private const val PLAY_POSITION_SAMPLE_PERIOD_MS = 5000L

        /**
         * How much will be skipped when skipping backward or forward.
         */
        private const val REWIND_TIME_MS = 10_000
    }
}