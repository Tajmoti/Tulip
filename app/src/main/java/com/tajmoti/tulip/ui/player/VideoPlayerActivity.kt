package com.tajmoti.tulip.ui.player

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tajmoti.libtulip.model.info.StreamableInfo
import com.tajmoti.libtulip.model.stream.UnloadedVideoWithLanguage
import com.tajmoti.libtulip.ui.player.Position
import com.tajmoti.libtulip.ui.player.VideoPlayerViewModel
import com.tajmoti.libtulip.ui.streams.FailedLink
import com.tajmoti.libtulip.ui.streams.LoadedLink
import com.tajmoti.libtulip.ui.streams.SelectedLink
import com.tajmoti.libtulip.ui.streams.StreamsViewModel
import com.tajmoti.tulip.R
import com.tajmoti.tulip.databinding.ActivityVideoPlayerBinding
import com.tajmoti.tulip.ui.*
import dagger.hilt.android.AndroidEntryPoint
import org.videolan.libvlc.LibVLC
import java.io.File
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class VideoPlayerActivity : BaseActivity<ActivityVideoPlayerBinding>(
    R.layout.activity_video_player
) {
    private val playerViewModel by viewModelsDelegated<VideoPlayerViewModel, AndroidVideoPlayerViewModel>()
    private val streamsViewModel by viewModelsDelegated<StreamsViewModel, AndroidStreamsViewModel>()

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
        binding.viewModel = playerViewModel
        binding.streamsViewModel = streamsViewModel
        binding.includeStreamsSelection.viewModel = streamsViewModel
        libVLC = LibVLC(this, arrayListOf("-vvv"))

        val adapter = StreamsAdapter(this::onStreamClickedDownload)
        adapter.callback = this::onStreamClickedPlay
        setupPlayerUi(adapter)
        setupFlowCollectors(adapter)
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        setupFullscreen()
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
            streamsViewModel.directLoaded.value?.let { reloadVideo(it.directLink) }
        }
        binding.buttonChangeSource.setOnClickListener {
            binding.includeStreamsSelection.containerStreamSelection.isVisible = true
        }
        binding.videoLayout.setOnClickListener {
            onVideoClicked()
        }
        binding.buttonBack.setOnClickListener {
            finish()
        }
        binding.includeStreamsSelection.titleStreamSelection.setOnClickListener {
            binding.includeStreamsSelection.containerStreamSelection.isVisible = false
        }
        binding.includeStreamsSelection.recyclerSearch.setupWithAdapterAndDivider(adapter)
    }

    private fun setupFlowCollectors(adapter: StreamsAdapter) {
        consume(playerViewModel.subtitleFile) { it?.let { onSubtitlesReady(it) } }
        consume(playerViewModel.downloadingError) { if (it) toast(R.string.subtitle_download_failure) }
        consume(playerViewModel.subtitleOffset, this::onSubtitlesDelayChanged)
        consume(playerViewModel.showPlayButton, this::updatePlayPauseButton)
        consume(playerViewModel.buffering, this::updateBuffering)
        consume(playerViewModel.position, this::updatePosition)
        consume(streamsViewModel.linksResult) { it?.let { adapter.items = it.streams } }
        consume(streamsViewModel.directLoadingUnsupported, this::onDirectLinkUnsupported)
        consume(streamsViewModel.directLoaded) { onDirectLinkLoaded(it) }
        consume(streamsViewModel.linkLoadingError, this::onDirectLinkLoadingError)
        consume(streamsViewModel.streamableInfo, this::onStreamableInfo)
    }

    override fun onStart() {
        super.onStart()
        vlc?.attachAndPlay(binding.videoLayout)
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
        vlc?.release()
        libVLC.release()
    }

    /**
     * Info about the streamable is known, show its name in the UI.
     */
    private fun onStreamableInfo(info: StreamableInfo?) {
        val name = when (info) {
            is StreamableInfo.Episode -> showToDisplayName(info)
            is StreamableInfo.Movie -> info.name
            null -> ""
        }
        binding.includeStreamsSelection.titleStreamSelection.text = name
        binding.textItemName.text = name
    }

    private fun showToDisplayName(item: StreamableInfo.Episode): String {
        val showSeasonEpNum = "${item.showName} S${item.seasonNumber}:E${item.info.number}"
        val episodeName = item.info.name?.let { " '$it'" } ?: ""
        return showSeasonEpNum + episodeName
    }

    /**
     * A video link was clicked, load it and play it.
     */
    private fun onStreamClickedPlay(stream: UnloadedVideoWithLanguage) {
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
     * that means we can start playing or downloading.
     */
    private fun onDirectLinkLoaded(it: LoadedLink?) {
        if (it == null) {
            binding.buttonRestartVideo.isVisible = false
            return
        }
        binding.buttonRestartVideo.isVisible = true
        if (!it.download) {
            startVideo(it.directLink, true)
        } else {
            toast(R.string.starting_download)
        }
    }

    /**
     * A link was clicked, but we can't extract a link to a direct video stream.
     * We can play it in a browser, but definitely can't download it.
     */
    private fun onDirectLinkUnsupported(it: SelectedLink) {
        if (!it.download) {
            startVideo(it.stream.url, false)
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
        if (link.download) {
            Toast.makeText(this, R.string.direct_loading_failure, Toast.LENGTH_SHORT)
                .show()
        } else {
            MaterialAlertDialogBuilder(this)
                .setIcon(R.drawable.ic_sad_24)
                .setTitle(R.string.direct_loading_failure)
                .setMessage(R.string.direct_loading_failure_message)
                .setPositiveButton(R.string.direct_loading_failure_yes) { _, _ ->
                    startVideo(link.stream.url, false)
                }
                .setNegativeButton(R.string.direct_loading_failure_no) { _, _ -> }
                .show()
        }
    }

    /**
     * A link was clicked and its redirects resolved.
     * If we also extracted a direct video link, play it here,
     * otherwise open a web browser.
     */
    private fun startVideo(url: String, direct: Boolean) {
        if (direct) {
            reloadVideo(url)
        } else {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                toast(R.string.web_browser_not_installed)
            }
            binding
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

    private fun formatTimeForDisplay(timeMs: Long): String {
        var mutableTimeMs = timeMs
        val hours = TimeUnit.MILLISECONDS.toHours(mutableTimeMs)
        mutableTimeMs -= TimeUnit.HOURS.toMillis(hours)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(mutableTimeMs)
        mutableTimeMs -= TimeUnit.MINUTES.toMillis(minutes)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(mutableTimeMs)
        return when {
            hours > 0 -> hours.toString() + timePad(minutes) + ':' + timePad(seconds)
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
        vlc?.playOrResume()
    }

    private fun setMediaProgress(progress: Int) {
        vlc?.progress = convertFromUiProgress(progress)
    }

    private fun showSubtitleSelectionDialog() {
        val subtitles = playerViewModel.subtitleList.value.sortedBy { it.language }
        val labels = subtitles
            .mapIndexed { index, item -> "#$index ${item.language}" }
            .toTypedArray()
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.select_subtitles)
            .setItems(labels) { _, index ->
                playerViewModel.onSubtitlesSelected(subtitles[index])
            }
            .setNegativeButton(R.string.back, null)
            .show()
    }

    private fun onSubtitlesReady(file: File) {
        vlc?.setSubtitles(Uri.fromFile(file).toString())
    }

    private fun onSubtitlesDelayChanged(delay: Long) {
        if (delay != 0L)
            toast("Applying subtitle delay of $delay ms")
        if (vlc?.setSubtitleDelay(delay) == false)
            toast("Setting of subtitle delay failed")
    }

    private fun onVideoClicked() {
        binding.groupVideoControls.isVisible = !binding.groupVideoControls.isVisible
    }

    private fun reloadVideo(url: String) {
        vlc?.release()
        vlc = VlcMediaHelper(libVLC, url)
            .also { it.attachAndPlay(binding.videoLayout) }
            .also { playerViewModel.onMediaAttached(it) }
        playerViewModel.subtitleFile.value?.let { onSubtitlesReady(it) }
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