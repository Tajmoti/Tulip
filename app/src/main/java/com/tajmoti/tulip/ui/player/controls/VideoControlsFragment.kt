package com.tajmoti.tulip.ui.player.controls

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tajmoti.libtulip.model.info.StreamableInfo
import com.tajmoti.libtulip.model.info.TulipCompleteEpisodeInfo
import com.tajmoti.libtulip.model.info.TulipMovie
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.subtitle.SubtitleInfo
import com.tajmoti.libtulip.ui.player.MediaPlayerHelper
import com.tajmoti.libtulip.ui.player.Position
import com.tajmoti.libtulip.ui.player.VideoPlayerViewModel
import com.tajmoti.libtulip.ui.streams.StreamsViewModel
import com.tajmoti.tulip.R
import com.tajmoti.tulip.databinding.FragmentVideoControlsBinding
import com.tajmoti.tulip.ui.BaseFragment
import com.tajmoti.tulip.ui.activityViewModelsDelegated
import com.tajmoti.tulip.ui.consume
import com.tajmoti.tulip.ui.player.*
import com.tajmoti.tulip.ui.player.episodes.EpisodesFragment
import com.tajmoti.tulip.ui.player.streams.AndroidStreamsViewModel
import com.tajmoti.tulip.ui.player.streams.StreamsFragment
import com.tajmoti.tulip.ui.player.subtitles.SubtitleItem
import com.tajmoti.tulip.ui.player.subtitles.SubtitleLanguageHeaderItem
import com.tajmoti.tulip.ui.showToDisplayName
import com.xwray.groupie.ExpandableGroup
import com.xwray.groupie.Group
import com.xwray.groupie.GroupieAdapter
import java.util.*
import java.util.concurrent.TimeUnit

class VideoControlsFragment :
    BaseFragment<FragmentVideoControlsBinding>(FragmentVideoControlsBinding::inflate) {
    private val playerViewModel by activityViewModelsDelegated<VideoPlayerViewModel, AndroidVideoPlayerViewModel>()
    private val streamsViewModel by activityViewModelsDelegated<StreamsViewModel, AndroidStreamsViewModel>()

    private val player: MediaPlayerHelper?
        get() = (activity as? VideoPlayerActivity)?.player


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = playerViewModel
        binding.streamsViewModel = streamsViewModel

        setupPlayerUi()
        setupFlowCollectors()
    }

    private fun setupPlayerUi() {
        binding.seekBarVideoProgress.max = UI_PROGRESS_STEPS
        binding.seekBarVideoProgress.setOnSeekBarChangeListener(OnSeekBarChangeListener())
        binding.buttonPlayResume.setOnClickListener {
            onPlayPausePressed()
        }
        binding.buttonRewind.setOnClickListener {
            player?.let { it.time = (it.time - REWIND_TIME_MS).coerceAtLeast(0) }
        }
        binding.buttonSeek.setOnClickListener {
            player?.let { it.time = (it.time + REWIND_TIME_MS).coerceAtMost(it.length) }
        }
        binding.buttonSubtitles.setOnClickListener {
            showSubtitleSelectionDialog()
        }
        binding.buttonSubtitleAdjustText.setOnClickListener {
            player?.let { playerViewModel.onTextSeen(it.time) }
        }
        binding.buttonSubtitleAdjustVideo.setOnClickListener {
            player?.let { playerViewModel.onWordHeard(it.time) }
        }
        binding.buttonRestartVideo.setOnClickListener {
            (requireActivity() as VideoPlayerActivity)
                .onVideoToPlayChanged(streamsViewModel.videoLinkToPlay.value, forceReload = true)
        }
        binding.buttonChangeSource.setOnClickListener {
            showStreamsSelection()
        }
        binding.buttonEpisodeList.setOnClickListener {
            showEpisodeSelection()
        }
        binding.buttonBack.setOnClickListener {
            (requireActivity() as VideoPlayerActivity)
                .switchToPipModeIfAvailable(true)
        }
    }

    private fun setupFlowCollectors() {
        consume(playerViewModel.showPlayButton, this::updatePlayPauseButton)
        consume(playerViewModel.position, this::updatePosition)
        consume(streamsViewModel.streamableInfo, this::onStreamableInfo)
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

    private fun showStreamsSelection() {
        popInCustomFragment(StreamsFragment())
    }

    private fun showEpisodeSelection() {
        val tvShowKey = (playerViewModel.streamableKey.value as EpisodeKey)
            .seasonKey
            .tvShowKey
        val args = Bundle()
            .apply { putSerializable(EpisodesFragment.ARG_TV_SHOW_KEY, tvShowKey) }
        val frag = EpisodesFragment()
            .apply { arguments = args }
        popInCustomFragment(frag)
    }

    private fun popInCustomFragment(frag: Fragment) {
        parentFragmentManager.commit {
            setCustomAnimations(
                R.anim.slide_from_top_enter,
                R.anim.slide_from_top_exit,
                R.anim.slide_from_top_enter,
                R.anim.slide_from_top_exit
            )
            replace(R.id.container_fragment_streams, frag)
        }
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

    private fun updatePosition(position: Position?) {
        val hasPosition = position != null
        binding.seekBarVideoProgress.visibility = if (hasPosition) View.VISIBLE else View.INVISIBLE
        position?.let {
            binding.seekBarVideoProgress.progress =
                convertToUiProgress(it.fraction)
        }
        position?.let { binding.textVideoTime.text = formatTimeForDisplay(it.timeMs) }
    }

    private fun onPlayPausePressed() {
        player?.playOrPause()
    }

    private fun setMediaProgress(progress: Int) {
        player?.progress = convertFromUiProgress(progress)
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
        dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.select_subtitles)
            .setView(recycler)
            .setNegativeButton(R.string.back, null)
            .show()
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        binding.groupVideoControls.isVisible = !isInPictureInPictureMode
        super.onPictureInPictureModeChanged(isInPictureInPictureMode)
    }

    private fun setupSubtitleRecycler(its: List<Group>): RecyclerView {
        val ctx = requireContext()
        val recycler = RecyclerView(ctx)
        val adapter = GroupieAdapter()
        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(ctx)
        recycler.addItemDecoration(DividerItemDecoration(ctx, DividerItemDecoration.VERTICAL))
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

    /**
     * Info about the streamable is known, show its name in the UI.
     */
    private fun onStreamableInfo(info: StreamableInfo?) {
        val name = when (info) {
            is TulipCompleteEpisodeInfo -> showToDisplayName(info)
            is TulipMovie -> info.name
            null -> ""
        }
        binding.textItemName.text = name
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
         * How much will be skipped when skipping backward or forward
         */
        private const val REWIND_TIME_MS = 10_000
    }
}