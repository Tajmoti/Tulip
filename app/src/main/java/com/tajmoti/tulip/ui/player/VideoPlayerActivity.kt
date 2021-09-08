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
import com.tajmoti.commonutils.logger
import com.tajmoti.tulip.R
import com.tajmoti.tulip.databinding.ActivityVideoPlayerBinding
import com.tajmoti.tulip.ui.BaseActivity
import com.tajmoti.tulip.ui.consume
import com.tajmoti.tulip.ui.toast
import dagger.hilt.android.AndroidEntryPoint
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.Media.Slave.Type.Subtitle
import org.videolan.libvlc.MediaPlayer
import java.io.File

@AndroidEntryPoint
class VideoPlayerActivity : BaseActivity<ActivityVideoPlayerBinding>(
    R.layout.activity_video_player
) {
    private val args: VideoPlayerActivityArgs by navArgs()
    private val viewModel: VideoPlayerViewModel by viewModels()

    /**
     * Instance of the VLC library and its video player
     */
    private lateinit var vlc: VLC


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowInsetsControllerCompat(window, binding.root)
            .apply { hide(WindowInsetsCompat.Type.systemBars()) }
            .apply {
                systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }

        vlc = initVLC()
        vlc.player.setEventListener { onVlcEvent(it) }
        binding.viewModel = viewModel
        binding.seekBarVideoProgress.max = UI_PROGRESS_STEPS
        binding.progressBarBuffering.max = UI_PROGRESS_STEPS
        binding.seekBarVideoProgress.setOnSeekBarChangeListener(OnSeekBarChangeListener())
        binding.buttonPlayResume.setOnClickListener { onPlayPausePressed() }
        binding.buttonSubtitles.setOnClickListener { showSubtitleSelectionDialog() }
        binding.buttonSubtitleAdjustText.setOnClickListener {
            viewModel.onTextSeen(vlc.player.time)
        }
        binding.buttonSubtitleAdjustVideo.setOnClickListener {
            viewModel.onWordHeard(vlc.player.time)
        }
        consume(viewModel.subtitleFile) { it?.let { onSubtitlesReady(it) } }
        consume(viewModel.downloadingError) { if (it) toast(R.string.subtitle_download_failure) }
        consume(viewModel.subtitleOffset) { onSubtitlesDelayChanged(it) }
    }

    private fun initVLC(): VLC {
        val libVlc = LibVLC(this, arrayListOf("-vvv"))
        val player = MediaPlayer(libVlc)
        val media = initMedia(libVlc)
        player.media = media
        media.release()
        return VLC(libVlc, player)
    }

    private fun initMedia(lib: LibVLC): Media {
        return Media(lib, Uri.parse(args.videoUrl))
    }

    override fun onStart() {
        super.onStart()
        vlc.player.attachViews(binding.videoLayout, null, true, false)
        vlc.player.play()
    }

    override fun onStop() {
        super.onStop()
        vlc.player.pause()
        vlc.player.detachViews()
    }

    override fun onDestroy() {
        super.onDestroy()
        vlc.release()
    }

    private fun onVlcEvent(it: MediaPlayer.Event) {
        if (!it.isSpam)
            logger.debug("VLC event ${it.format()}")
        when (it.type) {
            MediaPlayer.Event.PositionChanged -> updatePositionView(it.positionChanged)
            MediaPlayer.Event.Buffering -> updateBufferingView(it.buffering)
            MediaPlayer.Event.Playing -> updatePlayingView(playable = true, playing = true)
            MediaPlayer.Event.Paused -> updatePlayingView(playable = true, playing = false)
            MediaPlayer.Event.Stopped -> updatePlayingView(playable = false, playing = false)
            MediaPlayer.Event.EncounteredError -> updateErrorView()
        }
    }

    private fun updatePositionView(position: Float) {
        val hasPosition = position != 0.0f
        binding.seekBarVideoProgress.visibility = if (hasPosition) View.VISIBLE else View.INVISIBLE
        binding.seekBarVideoProgress.progress = convertToUiProgress(position)
    }

    private fun updateBufferingView(buffering: Float) {
        val shouldShow = buffering >= 0.0f && buffering < 100.0f
        val indeterminate = buffering == 0.0f
        binding.progressBarBuffering.isVisible = false
        if (!shouldShow)
            return
        binding.progressBarBuffering.isIndeterminate = indeterminate
        binding.progressBarBuffering.progress = convertToUiProgress(buffering)
        binding.progressBarBuffering.isVisible = true
    }

    private fun updatePlayingView(playable: Boolean, playing: Boolean) {
        val visibility = if (playable) View.VISIBLE else View.INVISIBLE
        binding.buttonPlayResume.visibility = visibility
        binding.buttonPlayResume.setImageResource(
            if (playing) {
                R.drawable.ic_baseline_pause_circle_outline_24
            } else {
                R.drawable.ic_baseline_play_circle_outline_24
            }
        )
        binding.seekBarVideoProgress.visibility = visibility
    }

    private fun updateErrorView() {
        updateBufferingView(100.0f)
        binding.imageVideoError.isVisible = true
    }

    private fun onPlayPausePressed() {
        if (vlc.player.isPlaying) {
            vlc.player.pause()
        } else {
            vlc.player.play()
        }
    }

    private fun setMediaProgress(progress: Int) {
        vlc.player.time = (convertFromUiProgress(progress) * vlc.player.length).toLong()
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
        vlc.player.addSlave(Subtitle, Uri.fromFile(file), true)
    }

    private fun onSubtitlesDelayChanged(delay: Long) {
        if (!vlc.player.isPlaying)
            return
        if (!vlc.player.setSpuDelay(-delay * 1000))
            toast("Setting of subtitle delay failed")
        if (delay != 0L)
            toast("Applying subtitle delay of $delay ms")
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

    class VLC(
        private val lib: LibVLC,
        val player: MediaPlayer
    ) {
        fun release() {
            player.release()
            lib.release()
        }
    }
}