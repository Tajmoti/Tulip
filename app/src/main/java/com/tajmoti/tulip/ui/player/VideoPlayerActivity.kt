package com.tajmoti.tulip.ui.player

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.navigation.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tajmoti.tulip.R
import com.tajmoti.tulip.databinding.ActivityVideoPlayerBinding
import com.tajmoti.tulip.ui.BaseActivity
import com.tajmoti.tulip.ui.consume
import com.tajmoti.tulip.ui.toast
import dagger.hilt.android.AndroidEntryPoint
import org.videolan.libvlc.LibVLC
import java.io.File
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class VideoPlayerActivity : BaseActivity<ActivityVideoPlayerBinding>(
    R.layout.activity_video_player
) {
    private val args: VideoPlayerActivityArgs by navArgs()
    private val viewModel: VideoPlayerViewModelImpl by viewModels()

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
        binding.viewModel = viewModel
        WindowInsetsControllerCompat(window, binding.root)
            .apply { hide(WindowInsetsCompat.Type.systemBars()) }
            .apply {
                systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }

        libVLC = LibVLC(this, arrayListOf("-vvv"))
        reloadVideo()
        setupUI()
        consume(viewModel.subtitleFile) { it?.let { onSubtitlesReady(it) } }
        consume(viewModel.downloadingError) { if (it) toast(R.string.subtitle_download_failure) }
        consume(viewModel.subtitleOffset, this::onSubtitlesDelayChanged)
        consume(viewModel.showPlayButton, this::updatePlayPauseButton)
        consume(viewModel.buffering, this::updateBuffering)
        consume(viewModel.position, this::updatePosition)
    }

    private fun setupUI() {
        binding.seekBarVideoProgress.max = UI_PROGRESS_STEPS
        binding.progressBarBuffering.max = UI_PROGRESS_STEPS
        binding.seekBarVideoProgress.setOnSeekBarChangeListener(OnSeekBarChangeListener())
        binding.buttonPlayResume.setOnClickListener { onPlayPausePressed() }
        binding.buttonSubtitles.setOnClickListener { showSubtitleSelectionDialog() }
        binding.buttonSubtitleAdjustText.setOnClickListener {
            vlc?.let { viewModel.onTextSeen(it.getTime()) }
        }
        binding.buttonSubtitleAdjustVideo.setOnClickListener {
            vlc?.let { viewModel.onWordHeard(it.getTime()) }
        }
        binding.buttonRestartVideo.setOnClickListener { reloadVideo() }
        binding.videoLayout.setOnClickListener { onVideoClicked() }
        binding.buttonBack.setOnClickListener { finish() }
    }

    override fun onStart() {
        super.onStart()
        vlc?.attachAndPlay(binding.videoLayout)
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
        binding.progressBarBuffering.isVisible = false
        if (!shouldShow)
            return
        binding.progressBarBuffering.isIndeterminate = indeterminate
        binding.progressBarBuffering.progress = convertToUiProgress(buffering!!)
        binding.progressBarBuffering.isVisible = true
    }

    private fun onPlayPausePressed() {
        vlc?.playOrResume()
    }

    private fun setMediaProgress(progress: Int) {
        vlc?.setProgress(convertFromUiProgress(progress))
    }

    private fun showSubtitleSelectionDialog() {
        val subtitles = viewModel.subtitleList.value.sortedBy { it.language }
        val labels = subtitles
            .mapIndexed { index, item -> "#$index ${item.language}" }
            .toTypedArray()
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.select_subtitles)
            .setItems(labels) { _, index ->
                viewModel.onSubtitlesSelected(subtitles[index])
            }
            .setNegativeButton(R.string.back, null)
            .show()
    }

    private fun onSubtitlesReady(file: File) {
        val uri = Uri.fromFile(file)
        vlc?.setSubtitles(uri)
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

    private fun reloadVideo() {
        vlc?.release()
        vlc = VlcMediaHelper(libVLC, args.videoUrl)
            .also { viewModel.onMediaAttached(it) }
            .also { it.attachAndPlay(binding.videoLayout) }
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