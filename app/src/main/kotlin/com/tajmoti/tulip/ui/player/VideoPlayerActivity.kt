package com.tajmoti.tulip.ui.player

import android.app.PictureInPictureParams
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tajmoti.libtulip.model.stream.UnloadedVideoStreamRef
import com.tajmoti.libtulip.ui.player.VideoPlayer
import com.tajmoti.libtulip.ui.player.MediaPlayerState
import com.tajmoti.libtulip.ui.player.VideoPlayerViewModel
import com.tajmoti.libtulip.ui.streams.FailedLink
import com.tajmoti.libtulip.ui.streams.LoadedLink
import com.tajmoti.libtulip.ui.streams.SelectedLink
import com.tajmoti.tulip.R
import com.tajmoti.tulip.databinding.ActivityVideoPlayerBinding
import com.tajmoti.tulip.ui.*
import com.tajmoti.tulip.ui.captcha.CaptchaSolverActivity
import com.tajmoti.tulip.ui.player.controls.VideoControlsFragment
import com.tajmoti.tulip.ui.player.helper.AudioFocusApi26
import com.tajmoti.tulip.ui.player.helper.AudioFocusApiBelow26
import com.tajmoti.tulip.ui.player.helper.AudioFocusAwareVideoPlayer
import com.tajmoti.tulip.ui.player.helper.AndroidVlcVideoPlayer
import dagger.hilt.android.AndroidEntryPoint
import org.videolan.libvlc.LibVLC
import java.io.File

@AndroidEntryPoint
class VideoPlayerActivity : BaseActivity<ActivityVideoPlayerBinding>(
    R.layout.activity_video_player
) {
    private val playerViewModel by viewModelsDelegated<VideoPlayerViewModel, AndroidVideoPlayerViewModel>()

    /**
     * Media session used to control the video from PIP mode.
     * Non-null only if some media is actually playing.
     */
    private var mediaSession: MediaSessionCompat? = null

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
        setupFullscreenIfNotInPip()
        if (playerViewModel.isPlaying.value)
            videoControlsVisibility = false
    }

    /**
     * Instance of the VLC library
     */
    private lateinit var libVLC: LibVLC

    /**
     * Wrapper around a concrete media player implementation.
     */
    var player: VideoPlayer? = null
        set(value) {
            if (value != null) {
                val newValue = wrapInMediaFocusAware(value)
                setupMediaSession(newValue)
                field = newValue
            } else {
                player?.release()
                mediaSession?.release()
                mediaSession = null
            }
        }

    /**
     * Wrapper around a VLC media player implementation.
     * Use only for initialization and resource releasing specific to VLC!
     * For everything else use [player], which respects audio focus.
     */
    private var vlc: AndroidVlcVideoPlayer? = null
        set(value) {
            field = value
            player = value
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainHandler = Handler(mainLooper, EasyHandler(messageHandlers))
        binding.viewModel = playerViewModel
        libVLC = LibVLC(this)

        setupPlayerUi()
        setupFlowCollectors()
        rescheduleVideoControlAutoHide()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val args = VideoPlayerActivityArgs.fromBundle(intent.extras!!)
        playerViewModel.changeStreamable(args.streamableKey)
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        setupFullscreenIfNotInPip()
        rescheduleVideoControlAutoHide()
    }

    private fun setupFullscreenIfNotInPip() {
        if (isInPipModeCompat)
            return
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val ctl = WindowInsetsControllerCompat(window, binding.root)
        ctl.hide(WindowInsetsCompat.Type.systemBars())
        ctl.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    private fun setupPlayerUi() {
        binding.videoLayout.setOnClickListener {
            onVideoClicked()
        }
    }

    override fun onBackPressed() {
        if (!hideOverlayMenuIfVisible())
            switchToPipModeIfAvailable(true)
    }

    private fun hideOverlayMenuIfVisible(): Boolean {
        val fm = supportFragmentManager
        val existingFragment = fm
            .findFragmentById(R.id.container_fragment_overlay_menu)
        return if (existingFragment != null) {
            existingFragment.slideToBottomDismiss(fm)
            true
        } else {
            false
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        if (!hideOverlayMenuIfVisible())
            switchToPipModeIfAvailable(true)
        return true
    }

    override fun onUserLeaveHint() {
        hideOverlayMenuIfVisible()
        switchToPipModeIfAvailable(false)
    }

    fun switchToPipModeIfAvailable(shouldOtherwiseFinish: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N &&
            packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                enterPictureInPictureMode(PictureInPictureParams.Builder().build())
            } else {
                @Suppress("DEPRECATION")
                enterPictureInPictureMode()
            }
        } else if (shouldOtherwiseFinish) {
            finish()
        }
    }

    private fun setupFlowCollectors() {
        consume(playerViewModel.subtitleFile) { onSubtitlesChanged(it) }
        consume(playerViewModel.subtitleDownloadError) { if (it) toast(R.string.subtitle_download_failure) }
        consume(playerViewModel.subtitleOffset, this::onSubtitlesDelayChanged)
        consume(playerViewModel.isPlaying, this::onPlayingChanged)
        consume(playerViewModel.buffering, this::updateBuffering)
        consume(playerViewModel.directLoadingUnsupported, this::onDirectLinkUnsupported)
        consume(playerViewModel.videoLinkToPlay) { onVideoToPlayChanged(it) }
        consume(playerViewModel.videoLinkToDownload) { onVideoToDownloadChanged(it) }
        consume(playerViewModel.linkLoadingError, this::onDirectLinkLoadingError)
        consume(playerViewModel.mediaPlayerState) { onMediaStateChanged(it) }
    }

    private fun setupMediaSession(value: VideoPlayer) {
        val mediaSession = MediaSessionCompat(this, "Tulip")
        mediaSession.isActive = true
        mediaSession.setCallback(TulipMediaSessionCallback(value))
        this.mediaSession = mediaSession
    }

    private fun wrapInMediaFocusAware(player: VideoPlayer): AudioFocusAwareVideoPlayer {
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioFocusApi26(this, audioManager, player)
        } else {
            AudioFocusApiBelow26(this, audioManager, player)
        }
    }

    private fun onMediaStateChanged(it: MediaPlayerState) {
        val (state, pos) = appStateToAndroidState(it)
        val capabilities = PlaybackStateCompat.ACTION_SEEK_TO or
                PlaybackStateCompat.ACTION_PLAY or
                PlaybackStateCompat.ACTION_PAUSE
        mediaSession?.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setActions(capabilities)
                .setState(state, pos, 1.0f)
                .build()
        )
    }

    private fun appStateToAndroidState(it: MediaPlayerState) =
        when (it) {
            is MediaPlayerState.Buffering -> PlaybackStateCompat.STATE_BUFFERING to it.position.timeMs
            is MediaPlayerState.Error -> PlaybackStateCompat.STATE_ERROR to -1L
            is MediaPlayerState.Finished -> PlaybackStateCompat.STATE_NONE to -1L
            is MediaPlayerState.Idle -> PlaybackStateCompat.STATE_NONE to -1L
            is MediaPlayerState.Paused -> PlaybackStateCompat.STATE_PAUSED to it.position.timeMs
            is MediaPlayerState.Playing -> PlaybackStateCompat.STATE_PLAYING to it.position.timeMs
        }

    override fun onStart() {
        super.onStart()
        volumeControlStream = AudioManager.STREAM_MUSIC
        vlc?.attach(binding.videoLayout)
        mediaSession?.isActive = true
    }

    override fun onResume() {
        super.onResume()
        setupFullscreenIfNotInPip()
    }

    override fun onStop() {
        super.onStop()
        vlc?.detachAndPause()
        mediaSession?.isActive = false
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseMedia()
        libVLC.release()
        mainHandler.removeCallbacksAndMessages(null)
    }

    private fun rescheduleVideoControlAutoHide() {
        mainHandler.removeMessages(MESSAGE_HIDE_UI)
        mainHandler.sendEmptyMessageDelayed(MESSAGE_HIDE_UI, UI_HIDE_DELAY_MS)
    }

    /**
     * If not null, a link to a direct video stream was selected and loaded,
     * that means we can start playing.
     */
    fun onVideoToPlayChanged(it: LoadedLink?, forceReload: Boolean = false) {
        if (it?.directLink != null) {
            // Don't relaunch the video if it's already set TODO media should be in the ViewModel
            if (it.directLink == player?.videoUrl && !forceReload)
                return
            releaseMedia()
            vlc = AndroidVlcVideoPlayer(libVLC, it.directLink)
                .apply { attach(binding.videoLayout) }
                .also { playerViewModel.onMediaAttached(it) }
            player?.play()
            onSubtitlesChanged(playerViewModel.subtitleFile.value)
        } else {
            releaseMedia()
        }
    }

    private fun releaseMedia() {
        playerViewModel.onMediaDetached()
        player?.release()
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
                    val ref = UnloadedVideoStreamRef(link.stream, true, link.languageCode)
                    playerViewModel.onStreamClicked(ref, link.download)
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

    private fun onPlayingChanged(playing: Boolean) {
        if (playing)
            rescheduleVideoControlAutoHide()
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
            binding.progressBarBuffering.setProgressFractionCompat(buffering!!, true)
        }
    }

    private fun onSubtitlesChanged(file: File?) {
        if (file != null) {
            val uri = Uri.fromFile(file).toString()
            player?.setSubtitles(VideoPlayer.SubtitleInfo(uri))
        } else {
            player?.setSubtitles(null)
        }
    }

    private fun onSubtitlesDelayChanged(delay: Long) {
        if (player?.setSubtitleDelay(delay) == false)
            toast(R.string.subtitle_delay_failed)
    }

    private fun onVideoClicked() {
        if (!isInPipModeCompat)
            videoControlsVisibility = !videoControlsVisibility
    }

    private var videoControlsVisibility: Boolean
        get() {
            return supportFragmentManager.findFragmentById(R.id.container_fragment_controls) != null
        }
        set(value) = run {
            supportFragmentManager.commit {
                if (value) {
                    replace(R.id.container_fragment_controls, VideoControlsFragment())
                } else {
                    supportFragmentManager.findFragmentById(R.id.container_fragment_controls)
                        ?.let { remove(it) }
                }
            }
        }

    companion object {
        private const val MESSAGE_HIDE_UI = 1

        /**
         * After how many seconds the UI will be hidden
         */
        private const val UI_HIDE_DELAY_MS = 5000L
    }
}