package com.tajmoti.tulip.ui.player

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tajmoti.libtulip.model.info.StreamableInfo
import com.tajmoti.libtulip.model.info.TulipCompleteEpisodeInfo
import com.tajmoti.libtulip.model.info.TulipEpisodeInfo
import com.tajmoti.libtulip.model.info.TulipMovie
import com.tajmoti.libtulip.model.stream.UnloadedVideoStreamRef
import com.tajmoti.libtulip.model.stream.UnloadedVideoWithLanguage
import com.tajmoti.libtulip.model.subtitle.SubtitleInfo
import com.tajmoti.libtulip.ui.player.MediaPlayerHelper
import com.tajmoti.libtulip.ui.player.Position
import com.tajmoti.libtulip.ui.player.VideoPlayerViewModel
import com.tajmoti.libtulip.ui.streams.FailedLink
import com.tajmoti.libtulip.ui.streams.LoadedLink
import com.tajmoti.libtulip.ui.streams.SelectedLink
import com.tajmoti.libtulip.ui.streams.StreamsViewModel
import com.tajmoti.tulip.R
import com.tajmoti.tulip.databinding.ActivityVideoPlayerBinding
import com.tajmoti.tulip.ui.*
import com.tajmoti.tulip.ui.captcha.CaptchaSolverActivity
import com.tajmoti.tulip.ui.player.streams.AndroidStreamsViewModel
import com.tajmoti.tulip.ui.player.streams.StreamsAdapter
import com.tajmoti.tulip.ui.player.subtitles.SubtitleItem
import com.tajmoti.tulip.ui.player.subtitles.SubtitleLanguageHeaderItem
import com.xwray.groupie.ExpandableGroup
import com.xwray.groupie.Group
import com.xwray.groupie.GroupieAdapter
import dagger.hilt.android.AndroidEntryPoint
import org.videolan.libvlc.LibVLC
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class VideoPlayerActivity : BaseActivity<ActivityVideoPlayerBinding>(
        R.layout.activity_video_player
) {
    private val playerViewModel by viewModelsDelegated<VideoPlayerViewModel, AndroidVideoPlayerViewModel>()
    private val streamsViewModel by viewModelsDelegated<StreamsViewModel, AndroidStreamsViewModel>()

    /**
     * Handler used for hiding UI after a timeout
     */
    private lateinit var mainHandler: Handler

    /**
     * Function to be called for each registered message
     */
    private val messageHandlers = mapOf<Int, (Message) -> Unit>(
        MESSAGE_HIDE_UI to { uiHider() }
    )

    /**
     * Hides the UI after a while if the video is playing or buffering
     */
    private val uiHider: () -> Unit = {
        setupFullscreen()
        if (playerViewModel.isPlaying.value)
            binding.groupVideoControls.isVisible = false
    }

    /**
     * Instance of the VLC library
     */
    private lateinit var libVLC: LibVLC

    /**
     * Media currently being played
     */
    private var vlc: VlcMediaHelper? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainHandler = Handler(mainLooper, EasyHandler(messageHandlers))
        binding.viewModel = playerViewModel
        binding.streamsViewModel = streamsViewModel
        libVLC = LibVLC(this, arrayListOf("-vvv"))

        val adapter = StreamsAdapter(this::onStreamClickedPlay, this::onStreamClickedDownload)
        setupPlayerUi(adapter)
        setupFlowCollectors(adapter)
        rescheduleVideoControlAutoHide()
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        setupFullscreen()
        rescheduleVideoControlAutoHide()
    }

    private fun setupFullscreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val ctl = WindowInsetsControllerCompat(window, binding.root)
        ctl.hide(WindowInsetsCompat.Type.systemBars())
        ctl.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    private fun setupPlayerUi(adapter: StreamsAdapter) {
        binding.seekBarVideoProgress.max = UI_PROGRESS_STEPS
        binding.progressBarBuffering.max = UI_PROGRESS_STEPS
        binding.seekBarVideoProgress.setOnSeekBarChangeListener(OnSeekBarChangeListener())
        binding.buttonPlayResume.setOnClickListener {
            onPlayPausePressed()
        }
        binding.buttonRewind.setOnClickListener {
            vlc?.let { it.time = (it.time - REWIND_TIME_MS).coerceAtLeast(0) }
        }
        binding.buttonSeek.setOnClickListener {
            vlc?.let { it.time = (it.time + REWIND_TIME_MS).coerceAtMost(it.length) }
        }
        binding.buttonSubtitles.setOnClickListener {
            showSubtitleSelectionDialog()
        }
        binding.buttonSubtitleAdjustText.setOnClickListener {
            vlc?.let { playerViewModel.onTextSeen(it.time) }
        }
        binding.buttonSubtitleAdjustVideo.setOnClickListener {
            vlc?.let { playerViewModel.onWordHeard(it.time) }
        }
        binding.buttonRestartVideo.setOnClickListener {
            onVideoToPlayChanged(streamsViewModel.videoLinkToPlay.value, forceReload = true)
        }
        binding.buttonChangeSource.setOnClickListener {
            binding.containerStreamSelection.isVisible = true
        }
        binding.videoLayout.setOnClickListener {
            onVideoClicked()
        }
        binding.buttonBack.setOnClickListener {
            finish()
        }
        binding.recyclerSearch.setupWithAdapterAndDivider(adapter)
        binding.buttonBackStreams.setOnClickListener {
            binding.containerStreamSelection.isVisible = false
        }
    }

    private fun setupFlowCollectors(adapter: StreamsAdapter) {
        consume(playerViewModel.subtitleFile) { onSubtitlesChanged(it) }
        consume(playerViewModel.downloadingError) { if (it) toast(R.string.subtitle_download_failure) }
        consume(playerViewModel.subtitleOffset, this::onSubtitlesDelayChanged)
        consume(playerViewModel.showPlayButton, this::updatePlayPauseButton)
        consume(playerViewModel.isPlaying, this::onPlayingChanged)
        consume(playerViewModel.buffering, this::updateBuffering)
        consume(playerViewModel.position, this::updatePosition)
        consume(playerViewModel.isDonePlaying, this::onDonePlayingChanged)
        consume(streamsViewModel.linksResult) { it?.let { adapter.items = it.streams } }
        consume(streamsViewModel.directLoadingUnsupported, this::onDirectLinkUnsupported)
        consume(streamsViewModel.videoLinkToPlay) { onVideoToPlayChanged(it) }
        consume(streamsViewModel.videoLinkToDownload) { onVideoToDownloadChanged(it) }
        consume(streamsViewModel.linkLoadingError, this::onDirectLinkLoadingError)
        consume(streamsViewModel.streamableInfo, this::onStreamableInfo)
    }

    override fun onStart() {
        super.onStart()
        vlc?.attach(binding.videoLayout)
    }

    override fun onResume() {
        super.onResume()
        setupFullscreen()
    }

    override fun onStop() {
        super.onStop()
        vlc?.detachAndPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseMedia()
        libVLC.release()
    }

    private fun rescheduleVideoControlAutoHide() {
        mainHandler.removeMessages(MESSAGE_HIDE_UI)
        mainHandler.sendEmptyMessageDelayed(MESSAGE_HIDE_UI, UI_HIDE_DELAY_MS)
    }

    /**
     * Info about the streamable is known, show its name in the UI.
     */
    private fun onStreamableInfo(info: StreamableInfo?) {
        val name = when (info) {
            is TulipCompleteEpisodeInfo -> showToDisplayName(info)
            is TulipMovie -> info.name
            null -> ""
        }
        binding.titleStreamSelection.text = name
        binding.textItemName.text = name
    }

    private fun showToDisplayName(item: TulipCompleteEpisodeInfo): String {
        val showSeasonEpNum = "${item.showName} S${item.seasonNumber}:E${item.episodeNumber}"
        val episodeName = item.name?.let { " '$it'" } ?: ""
        return showSeasonEpNum + episodeName
    }

    /**
     * A video link was clicked, load it and play it.
     */
    private fun onStreamClickedPlay(stream: UnloadedVideoWithLanguage) {
        binding.containerStreamSelection.isVisible = false
        streamsViewModel.onStreamClicked(stream.video, false)
    }

    /**
     * A video link was long-clicked which means download.
     */
    private fun onStreamClickedDownload(stream: UnloadedVideoWithLanguage) {
        streamsViewModel.onStreamClicked(stream.video, true)
    }

    /**
     * If not null, a link to a direct video stream was selected and loaded,
     * that means we can start playing.
     */
    private fun onVideoToPlayChanged(it: LoadedLink?, forceReload: Boolean = false) {
        if (it?.directLink != null) {
            // Don't relaunch the video if it's already set TODO media should be in the ViewModel
            if (it.directLink == vlc?.videoUrl && !forceReload)
                return
            releaseMedia()
            vlc = VlcMediaHelper(libVLC, it.directLink)
                .apply { attach(binding.videoLayout) }
                .apply { playOrPause() }
                .also { playerViewModel.onMediaAttached(it) }
            onSubtitlesChanged(playerViewModel.subtitleFile.value)
        } else {
            releaseMedia()
        }
    }

    private fun releaseMedia() {
        playerViewModel.onMediaDetached()
        vlc?.release()
        vlc = null
    }

    /**
     * If not null, a link to a direct video stream was selected and loaded,
     * that means we can start downloading.
     */
    private fun onVideoToDownloadChanged(it: LoadedLink?) {
        if (it != null)
            toast(R.string.starting_download)
    }

    /**
     * A link was clicked, but we can't extract a link to a direct video stream.
     * We can play it in a browser, but definitely can't download it.
     */
    private fun onDirectLinkUnsupported(it: SelectedLink) {
        if (!it.download) {
            startVideoExternal(it.stream.url)
        } else {
            Toast.makeText(this, R.string.stream_not_downloadable, Toast.LENGTH_SHORT)
                .show()
        }
    }

    /**
     * A link was clicked, but we failed to resolve its redirects
     * or its direct video stream link.
     */
    private fun onDirectLinkLoadingError(link: FailedLink) {
        when {
            link.captchaInfo != null ->
                handleCaptcha(link)
            link.download ->
                Toast.makeText(this, R.string.direct_loading_failure, Toast.LENGTH_SHORT)
                    .show()
            else ->
                MaterialAlertDialogBuilder(this)
                    .setIcon(R.drawable.ic_sad_24)
                    .setTitle(R.string.direct_loading_failure)
                    .setMessage(R.string.direct_loading_failure_message)
                    .setPositiveButton(R.string.direct_loading_failure_yes) { _, _ ->
                        startVideoExternal(link.stream.url)
                    }
                    .setNegativeButton(R.string.direct_loading_failure_no) { _, _ -> }
                    .show()
        }
    }

    /**
     * Link which is waiting for its captcha to be solved.
     */
    private var lastLink: FailedLink? = null


    private val captchaSolverLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                toast(R.string.captcha_solved)
                lastLink?.let { link ->
                    val ref = UnloadedVideoStreamRef(link.stream, true)
                    streamsViewModel.onStreamClicked(ref, link.download)
                }
            } else {
                toast(R.string.captcha_not_solved)
            }
        }

    private fun handleCaptcha(link: FailedLink) {
        lastLink = link
        val info = link.captchaInfo!!
        val from = Uri.parse(info.captchaUrl)
        val to = Uri.parse(info.destinationUrl)
        val intent = CaptchaSolverActivity.newInstance(this, from, to)
        captchaSolverLauncher.launch(intent)
    }

    /**
     * Open a web browser to play a link to a hosting page.
     */
    private fun startVideoExternal(url: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            toast(R.string.web_browser_not_installed)
        }
    }

    private fun updatePlayPauseButton(it: VideoPlayerViewModel.PlayButtonState) {
        when (it) {
            VideoPlayerViewModel.PlayButtonState.SHOW_PLAY ->
                updatePlayPauseImage(false)
            VideoPlayerViewModel.PlayButtonState.SHOW_PAUSE ->
                updatePlayPauseImage(true)
            else -> Unit
        }
    }

    private fun onPlayingChanged(playing: Boolean) {
        if (playing)
            rescheduleVideoControlAutoHide()
    }

    private fun updatePlayPauseImage(showPause: Boolean) {
        binding.buttonPlayResume.setImageResource(
            if (showPause) {
                R.drawable.ic_baseline_pause_24
            } else {
                R.drawable.ic_baseline_play_arrow_24
            }
        )
    }

    private fun updatePosition(position: Position?) {
        val hasPosition = position != null
        binding.seekBarVideoProgress.visibility = if (hasPosition) View.VISIBLE else View.INVISIBLE
        position?.let { binding.seekBarVideoProgress.progress = convertToUiProgress(it.fraction) }
        position?.let { binding.textVideoTime.text = formatTimeForDisplay(it.timeMs) }
    }

    private fun onDonePlayingChanged(done: Boolean) {
        if (done)
            finish()
    }

    private fun formatTimeForDisplay(timeMs: Long): String {
        var mutableTimeMs = timeMs
        val hours = TimeUnit.MILLISECONDS.toHours(mutableTimeMs)
        mutableTimeMs -= TimeUnit.HOURS.toMillis(hours)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(mutableTimeMs)
        mutableTimeMs -= TimeUnit.MINUTES.toMillis(minutes)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(mutableTimeMs)
        return when {
            hours > 0 -> hours.toString() + ':' + timePad(minutes) + ':' + timePad(seconds)
            minutes > 0 -> timePad(minutes) + ':' + timePad(seconds)
            else -> "0:" + timePad(seconds)
        }
    }

    private fun timePad(time: Long): String {
        return time.toString().padStart(2, '0')
    }

    private fun updateBuffering(buffering: Float?) {
        val shouldShow = buffering != null && buffering >= 0.0f && buffering < 100.0f
        val indeterminate = buffering == 0.0f
        if (shouldShow != binding.progressBarBuffering.isVisible) {
            binding.progressBarBuffering.isVisible = shouldShow
        }
        if (!shouldShow)
            return
        if (indeterminate && !binding.progressBarBuffering.isIndeterminate) {
            binding.progressBarBuffering.isVisible = false
            binding.progressBarBuffering.isIndeterminate = true
            binding.progressBarBuffering.isVisible = true
        } else if (!indeterminate) {
            val progressSteps = convertToUiProgress(buffering!! / 100.0f)
            binding.progressBarBuffering.setProgressCompat(progressSteps, true)
        }
    }

    private fun onPlayPausePressed() {
        vlc?.playOrPause()
    }

    private fun setMediaProgress(progress: Int) {
        vlc?.progress = convertFromUiProgress(progress)
    }

    private fun showSubtitleSelectionDialog() {
        var dialog: Dialog? = null
        val callback: (SubtitleInfo?) -> Unit = {
            dialog?.dismiss()
            playerViewModel.onSubtitlesSelected(it)
        }
        val noSubItem = SubtitleItem(0, null, callback)
        val groups = createSubtitleGroups(playerViewModel.subtitleList.value, callback)
        val recycler = setupSubtitleRecycler(listOf(noSubItem) + groups)
        dialog = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.select_subtitles)
            .setView(recycler)
            .setNegativeButton(R.string.back, null)
            .show()
    }

    private fun setupSubtitleRecycler(its: List<Group>): RecyclerView {
        val recycler = RecyclerView(this)
        val adapter = GroupieAdapter()
        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        adapter.addAll(its)
        return recycler
    }

    private fun createSubtitleGroups(
        state: List<SubtitleInfo>,
        callback: (SubtitleInfo?) -> Unit
    ): List<Group> {
        val groupedByLanguage = state
            .map { Locale.forLanguageTag(it.language) to it }
            .sortedBy { it.first.displayLanguage }
            .groupBy { it.first }
        return groupedByLanguage
            .map {
                val subtitleInfo = it.value.map { pair -> pair.second }
                createSubtitleGroup(it.key, subtitleInfo, callback)
            }
    }

    private fun createSubtitleGroup(
        locale: Locale,
        season: List<SubtitleInfo>,
        callback: (SubtitleInfo?) -> Unit
    ): ExpandableGroup {
        val header = SubtitleLanguageHeaderItem(locale)
        val group = ExpandableGroup(header)
        val mapped = season.mapIndexed { index, info -> SubtitleItem(index, info, callback) }
        group.addAll(mapped)
        return group
    }

    private fun onSubtitlesChanged(file: File?) {
        if (file != null) {
            val uri = Uri.fromFile(file).toString()
            vlc?.setSubtitles(MediaPlayerHelper.SubtitleInfo(uri))
        } else {
            vlc?.setSubtitles(null)
        }
    }

    private fun onSubtitlesDelayChanged(delay: Long) {
        if (vlc?.setSubtitleDelay(delay) == false)
            toast(R.string.subtitle_delay_failed)
    }

    private fun onVideoClicked() {
        binding.groupVideoControls.isVisible = !binding.groupVideoControls.isVisible
    }

    inner class OnSeekBarChangeListener : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if (!fromUser)
                return
            setMediaProgress(progress)
        }

        override fun onStartTrackingTouch(p0: SeekBar?) {

        }

        override fun onStopTrackingTouch(p0: SeekBar?) {

        }
    }

    companion object {
        private const val MESSAGE_HIDE_UI = 1

        /**
         * After how many seconds the UI will be hidden
         */
        private const val UI_HIDE_DELAY_MS = 5000L

        /**
         * How much will be skipped when skipping backward or forward
         */
        private const val REWIND_TIME_MS = 10_000

        /**
         * The number of steps that the seek and progress bars have.
         */
        private const val UI_PROGRESS_STEPS = 1_000

        fun convertToUiProgress(realProgress: Float): Int {
            return (realProgress * UI_PROGRESS_STEPS).toInt()
        }

        fun convertFromUiProgress(uiProgress: Int): Float {
            return (uiProgress.toFloat() / UI_PROGRESS_STEPS)
        }
    }
}