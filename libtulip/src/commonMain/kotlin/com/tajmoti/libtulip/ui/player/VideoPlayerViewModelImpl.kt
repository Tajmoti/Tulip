package com.tajmoti.libtulip.ui.player

import com.tajmoti.commonutils.map
import com.tajmoti.commonutils.mapWith
import com.tajmoti.libtulip.PREFERRED_LANGUAGE
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
import com.tajmoti.libtulip.ui.streams.FailedLink
import com.tajmoti.libtulip.ui.streams.LoadedLink
import com.tajmoti.libtulip.ui.streams.SelectedLink
import com.tajmoti.libtulip.ui.videoComparator
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
    override val viewModelScope: CoroutineScope,
    streamableKeyInitial: StreamableKey,
) : VideoPlayerViewModel {
    private val streamableKeyImpl = MutableStateFlow(streamableKeyInitial)

    /**
     * List of episodes of the currently playing TV show or null if a movie is being played.
     */
    private val episodeList = streamableKeyImpl
        .flatMapLatest { getSeasonByKey(it) }
        .map { it?.data?.episodes }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)


    /**
     * Streamable key to loading state of the list of available streams.
     */
    private val keyWithLinkListLoadingState = streamableKeyImpl
        .flatMapLatest { key ->
            streamService.getStreamsByKey(key)
                .map(::mapStreamsResult)
                .map { key to it }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /**
     * Loading state of the list of available streams.
     */
    private val linkListLoadingState = keyWithLinkListLoadingState
        .filterNotNull()
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
        .filterNotNull()
        .mapNotNull { (key, state) -> (state as? LinkListLoadingState.Success)?.let { key to state.streams } }
        .filter { (_, streams) -> anyGoodStreams(streams) }
        .distinctUntilChangedBy { (key, _) -> key }
        .map { (_, streams) -> firstGoodStream(streams) to false }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    private fun firstGoodStream(it: List<UnloadedVideoStreamRef>) =
        it.first { video -> video.linkExtractionSupported }

    private fun anyGoodStreams(it: List<UnloadedVideoStreamRef>) =
        it.any { s -> s.linkExtractionSupported && s.language == PREFERRED_LANGUAGE }

    /**
     * The stream that should actually be played.
     */
    private val streamToPlay = merge(manualStream, autoStream)
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    /**
     * Loading state of a selected streaming service video
     */
    private val linkLoadingState = streamToPlay
        .flatMapLatest {
            it?.let { fetchStreams(it.first, it.second) }
                ?: flowOf(LinkLoadingState.Idle)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, LinkLoadingState.Idle)

    private val internalStreamableInfo = streamableKeyImpl
        .flatMapLatest { getStreamableInfo(it) }
        .map { it.getOrNull() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)


    /**
     * If a TV show episode is playing, this is its info.
     */
    private val tvShowData = combine(episodeList, streamableKeyImpl) { a, b -> a to b }
        .map { (episodes, key) -> episodes?.let { selectTvShowData(it, key) } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /**
     * State of subtitle list loading.
     */
    private val loadingSubtitlesState = streamableKeyImpl
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

    /**
     * Currently attached media player.
     */
    private val attachedMediaPlayer = MutableStateFlow<VideoPlayer?>(null)

    /**
     * State of the currently attached media player.
     */
    private val mediaPlayerStateImpl = attachedMediaPlayer
        .flatMapLatest { it?.state ?: flowOf(MediaPlayerState.Idle) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, MediaPlayerState.Idle)

    private val positionImpl = mediaPlayerStateImpl
        .map(viewModelScope) { state -> state.validPositionOrNull }

    /**
     * Progress of the currently playing media or null if nothing is currently playing.
     */
    private val progress = positionImpl
        .map { state -> state?.fraction }

    /**
     * Persisted playing position, to be used only to resume playback
     * state once the streamable is initially loaded.
     */
    private val persistedPlayingProgress = streamableKeyImpl
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
        combine(attachedMediaPlayer.filterNotNull(), streamableKeyImpl, persistedPlayingProgress)
        { player, key, keyToProgress -> Triple(player, key, keyToProgress) }
            .distinctUntilChanged { (p1, k1), (p2, k2) -> p1 == p2 && k1 == k2 }
            .filter { (_, key, keyToProgress) -> key == keyToProgress.first }
            .mapNotNull { (player, _, keyPos) -> keyPos.second?.let { player to it } }

    /**
     * State of the interactive subtitle synchronization mechanism.
     * If not null, the user has pressed a button because they
     * saw a word that they want to match to a later shown subtitle text or vice versa.
     */
    private val subSyncState = MutableStateFlow<SubtitleSyncState?>(null)

    private val isPlayingImpl = mediaPlayerStateImpl
        .map(viewModelScope) { it is MediaPlayerState.Playing }

    /**
     * The current playing position while the media is actively playing,
     * sampled each [PLAY_POSITION_SAMPLE_PERIOD_MS] milliseconds.
     */
    private val playingPositionToPersist = combine(streamableKeyImpl, progress, isPlayingImpl)
    { key, progress, playing -> Triple(key, progress, playing) }
        // This prevents an existing position from being applied to a newly selected streamable
        .distinctUntilChanged { (_, pos1), (_, pos2) -> pos1 == pos2 }
        .filter { (_, _, playing) -> playing }
        .sample(PLAY_POSITION_SAMPLE_PERIOD_MS)
        .mapNotNull { (key, progress) -> progress?.let { key to progress } }

    init {
        startPersistPlayingPosition()
        startRestorePlayingPosition()
    }

    override fun goToNextEpisode() {
        tvShowData.value?.nextEpisode?.let { changeStreamable(it) }
    }

    override fun changeStreamable(key: StreamableKey) {
        // This prevents the video from stopping if the currently playing streamable is selected
        if (streamableKeyImpl.value == key)
            return
        viewModelScope.launch {
            manualStream.emit(null)
            streamableKeyImpl.emit(key)
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
        val time = positionImpl.value?.timeMs ?: return
        val seen = subSyncState.value as? SubtitleSyncState.Seen
        if (seen == null) {
            val existingOffset = subSyncState.value?.offsetMs ?: 0L
            subSyncState.value = SubtitleSyncState.Heard(time, existingOffset)
        } else {
            calculateAndSetSubtitleDelay(time, seen.time)
        }
    }

    override fun onTextSeen() {
        val time = positionImpl.value?.timeMs ?: return
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
        val player = mediaPlayerState.value
        player.validPositionOrNull?.let { position ->
            player.durationOrNull?.let { duration ->
                attachedMediaPlayer.value?.setTime((position.timeMs + REWIND_TIME_MS).coerceAtMost(duration))
            }
        }
    }

    override fun skipBackwards() {
        // TODO Not the best solution, probably merge with playerProgressToRestore
        val player = mediaPlayerState.value
        player.validPositionOrNull?.let { position ->
            attachedMediaPlayer.value?.setTime((position.timeMs - REWIND_TIME_MS).coerceAtLeast(0))
        }
    }

    override fun playPause() {
        // TODO Not the best solution
        attachedMediaPlayer.value?.playOrPause()
    }

    override fun setPlaybackProgress(progress: Float) {
        // TODO Not the best solution
        attachedMediaPlayer.value?.setProgress(progress)
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
            playerProgressToRestore.collect { (player, pos) -> player.setProgress(pos) }
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

    private fun selectTvShowData(
        episodes: List<TulipEpisodeInfo>,
        key: StreamableKey,
    ): VideoPlayerViewModel.TvShowData {
        val currentEpisodeIndex = currentEpisodeIndex(episodes, key)
        val previous = currentEpisodeIndex
            ?.takeUnless { it == 0 }
            ?.let { episodes[it - 1].key }
        val next = currentEpisodeIndex
            ?.takeUnless { it == episodes.size - 1 }
            ?.let { episodes[it + 1].key }
        return VideoPlayerViewModel.TvShowData(previous, next)
    }

    private fun currentEpisodeIndex(episodes: List<TulipEpisodeInfo>, key: StreamableKey): Int? {
        return episodes
            .indexOfFirst { episode -> episode.key == key }
            .takeIf { it != -1 }
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
        downloadService.downloadFileToFiles(link, internalStreamableInfo.value!!)
    }

    private fun mapStreamsResult(result: StreamsResult): LinkListLoadingState {
        return when (result) {
            is StreamsResult.Success -> LinkListLoadingState.Success(sortStreams(result), result.possiblyFinished)
            is StreamsResult.Error -> LinkListLoadingState.Error
        }
    }

    private fun sortStreams(result: StreamsResult.Success): List<UnloadedVideoStreamRef> {
        return result.streams.sortedWith(videoComparator)
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
        /**
         * The video stream that was selected.
         */
        val stream: VideoStreamRef?

        /**
         * Whether the stream is to be downloaded (else played).
         */
        val download: Boolean

        object Idle : LinkLoadingState {
            override val stream: VideoStreamRef? = null
            override val download = false
        }

        /**
         * The streaming page URL is being resolved.
         */
        data class Loading(
            override val stream: VideoStreamRef.Unresolved,
            override val download: Boolean,
        ) : LinkLoadingState

        /**
         * Direct link extraction is not supported for the clicked streaming site.
         */
        data class DirectLinkUnsupported(
            override val stream: VideoStreamRef.Resolved,
            override val download: Boolean,
        ) : LinkLoadingState

        /**
         * A direct video link is being extracted.
         */
        data class LoadingDirect(
            override val stream: VideoStreamRef.Resolved,
            override val download: Boolean,
        ) : LinkLoadingState

        /**
         * A direct video link was extracted successfully.
         */
        data class LoadedDirect(
            override val stream: VideoStreamRef.Resolved,
            override val download: Boolean,
            val directLink: String,
        ) : LinkLoadingState

        data class Error(
            override val stream: VideoStreamRef,
            val languageCode: LanguageCode,
            override val download: Boolean,
            val captcha: CaptchaInfo?,
        ) : LinkLoadingState
    }


    private val internalState = com.tajmoti.commonutils.combine(
        streamableKeyImpl,
        internalStreamableInfo,
        tvShowData,
        linkListLoadingState,
        linkLoadingState,
        loadingSubtitlesState,
        subtitleDownloadState,
        mediaPlayerStateImpl,
        subSyncState
    ) { a, b, c, d, e, f, g, h, i ->
        InternalState(a, b, c, d, e, f, g, h, i)
    }.stateIn(viewModelScope, SharingStarted.Lazily, InternalState())

    companion object {
        /**
         * Playing position will be stored this often.
         */
        private const val PLAY_POSITION_SAMPLE_PERIOD_MS = 1000L

        /**
         * How much will be skipped when skipping backward or forward.
         */
        private const val REWIND_TIME_MS = 10_000
    }

    data class InternalState(
        /**
         * Key of the streamable that is currently being loaded or played.
         */
        val streamableKey: StreamableKey? = null,
        /**
         * Streamable info of [InternalState.streamableKey] or null if not yet available.
         */
        val streamableInfo: StreamableInfo? = null,
        /**
         * If this is a TV show, this is its information.
         */
        val tvShowData: VideoPlayerViewModel.TvShowData? = null,
        /**
         * Status of video link list loading.
         */
        val linkListLoadingState: LinkListLoadingState = LinkListLoadingState.Loading,
        /**
         * Status of loading of a selected link.
         */
        val linkLoadingState: LinkLoadingState = LinkLoadingState.Idle,
        /**
         * Status of subtitle list loading.
         */
        val loadingSubtitlesState: SubtitleListLoadingState = SubtitleListLoadingState.Loading,
        /**
         * Status of selected subtitle file downloading.
         */
        val subtitleDownloadState: SubtitleDownloadingState = SubtitleDownloadingState.Idle,
        /**
         * Media player state.
         */
        val playerState: MediaPlayerState = MediaPlayerState.Idle,
        /**
         * Subtitle synchronization state.
         */
        val subSyncState: SubtitleSyncState? = null,
    )

    override val state = internalState.mapWith(viewModelScope) {
        val streamLoadingFinalSuccessState = (linkListLoadingState as? LinkListLoadingState.Success)?.takeIf { success -> success.final }
        VideoPlayerViewModel.State(
            tvShowData = tvShowData,
            streamableKey = streamableKey,
            streamableInfo = streamableInfo,
            linkListState = VideoPlayerViewModel.LinkListState(
                linksLoading = linkListLoadingState is LinkListLoadingState.Loading || (linkListLoadingState is LinkListLoadingState.Success && linkListLoadingState.streams.isEmpty() && !(streamLoadingFinalSuccessState?.streams?.none() ?: false)),
                linksResult = (linkListLoadingState as? LinkListLoadingState.Success)?.streams,
                linksAnyResult = (linkListLoadingState as? LinkListLoadingState.Success)?.streams?.any() ?: false,
                linksNoResult = streamLoadingFinalSuccessState?.streams?.none() ?: false,
            ),
            selectedLinkState = VideoPlayerViewModel.SelectedLinkState(
                loadingStreamOrDirectLink = linkLoadingState is LinkLoadingState.Loading || linkLoadingState is LinkLoadingState.LoadingDirect,
                videoLinkPreparingOrPlaying = linkLoadingState.stream.takeIf { !linkLoadingState.download },
                videoLinkToPlay = (linkLoadingState as? LinkLoadingState.LoadedDirect)?.takeIf { !it.download }?.let { LoadedLink(it.stream, it.directLink) },
                videoLinkToDownload = (linkLoadingState as? LinkLoadingState.LoadedDirect)?.takeIf { it.download }?.let { LoadedLink(it.stream, it.directLink) },
                linkLoadingError = (linkLoadingState as? LinkLoadingState.Error)?.let { FailedLink(it.stream, it.languageCode, it.download, it.captcha) },
                directLoadingUnsupported = (linkLoadingState as? LinkLoadingState.DirectLinkUnsupported)?.let { SelectedLink(it.stream, it.download) }
            ),
            playbackState = VideoPlayerViewModel.PlaybackState(
                subtitleOffset = (subSyncState as? SubtitleSyncState.OffsetUsed)?.offsetMs ?: 0L,
                showPlayButton = playerStateToPlayButtonState(playerState),
                isPlaying = playerState is MediaPlayerState.Playing,
                buffering = (playerState as? MediaPlayerState.Buffering)?.percent,
                position = playerState.validPositionOrNull,
                isDonePlaying = playerState is MediaPlayerState.Finished,
                isError = playerState is MediaPlayerState.Error,
                mediaPlayerState = playerState
            ),
            subtitleState = VideoPlayerViewModel.SubtitleState(
                subtitleList = (loadingSubtitlesState as? SubtitleListLoadingState.Success)?.subtitles ?: emptyList(),
                loadingSubtitleList = loadingSubtitlesState is SubtitleListLoadingState.Loading,
                subtitlesReadyToSelect = loadingSubtitlesState is SubtitleListLoadingState.Success,
                downloadingSubtitles = subtitleDownloadState is SubtitleDownloadingState.Loading,
                subtitleDownloadError = subtitleDownloadState is SubtitleDownloadingState.Error,
                subtitleFile = (subtitleDownloadState as? SubtitleDownloadingState.Success)?.subtitles,
            )
        )
    }

    private fun playerStateToPlayButtonState(state: MediaPlayerState) = when (state) {
        is MediaPlayerState.Buffering -> VideoPlayerViewModel.PlayButtonState.HIDE
        is MediaPlayerState.Error -> VideoPlayerViewModel.PlayButtonState.HIDE
        is MediaPlayerState.Idle -> VideoPlayerViewModel.PlayButtonState.HIDE
        is MediaPlayerState.Paused -> VideoPlayerViewModel.PlayButtonState.SHOW_PLAY
        is MediaPlayerState.Playing -> VideoPlayerViewModel.PlayButtonState.SHOW_PAUSE
        is MediaPlayerState.Finished -> VideoPlayerViewModel.PlayButtonState.HIDE
    }
}