package com.tajmoti.tulip.ui.player.controls

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tajmoti.libtulip.dto.SubtitleDto
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.SubtitleKey
import com.tajmoti.libtulip.ui.player.VideoPlayerViewModel
import com.tajmoti.tulip.R
import com.tajmoti.tulip.databinding.FragmentVideoControlsBinding
import com.tajmoti.tulip.ui.base.BaseFragment
import com.tajmoti.tulip.ui.player.AndroidVideoPlayerViewModel
import com.tajmoti.tulip.ui.player.VideoPlayerActivity
import com.tajmoti.tulip.ui.player.episodes.EpisodesFragment
import com.tajmoti.tulip.ui.player.streams.StreamsFragment
import com.tajmoti.tulip.ui.player.subtitles.SubtitleItem
import com.tajmoti.tulip.ui.player.subtitles.SubtitleLanguageHeaderItem
import com.tajmoti.tulip.ui.utils.activityViewModelsDelegated
import com.xwray.groupie.ExpandableGroup
import com.xwray.groupie.Group
import com.xwray.groupie.GroupieAdapter
import java.util.*

class VideoControlsFragment : BaseFragment<FragmentVideoControlsBinding>(
    FragmentVideoControlsBinding::inflate
) {
    private val viewModel by activityViewModelsDelegated<VideoPlayerViewModel, AndroidVideoPlayerViewModel>()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        setupButtonListeners()
    }

    private fun setupButtonListeners() {
        for ((button, handler) in BUTTON_CLICK_ACTIONS)
            button.get(binding).setOnClickListener { handler(this) }
    }

    private fun switchToPipOrExit() {
        (requireActivity() as VideoPlayerActivity)
            .switchToPipModeIfAvailable(true)
    }

    private fun showStreamsSelection() {
        popInCustomFragment(StreamsFragment())
    }

    private fun showEpisodeSelection() {
        val tvShowKey = (viewModel.streamableKey.value as EpisodeKey)
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
            replace(R.id.container_fragment_overlay_menu, frag)
        }
    }

    private fun showSubtitleSelectionDialog() {
        var dialog: Dialog? = null
        val callback: (SubtitleKey?) -> Unit = {
            dialog?.dismiss()
            viewModel.onSubtitlesSelected(it)
        }
        val noSubItem = SubtitleItem(0, null, callback)
        val groups = createSubtitleGroups(viewModel.subtitleList.value, callback)
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
        state: List<SubtitleDto>,
        callback: (SubtitleKey?) -> Unit,
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
        season: List<SubtitleDto>,
        callback: (SubtitleKey?) -> Unit,
    ): ExpandableGroup {
        val header = SubtitleLanguageHeaderItem(locale)
        val group = ExpandableGroup(header)
        val mapped = season.mapIndexed { index, info -> SubtitleItem(index, info, callback) }
        group.addAll(mapped)
        return group
    }

    companion object {
        /**
         * Binding of buttons to activity actions.
         */
        private val BUTTON_CLICK_ACTIONS = mapOf(
            FragmentVideoControlsBinding::buttonSubtitles to VideoControlsFragment::showSubtitleSelectionDialog,
            FragmentVideoControlsBinding::buttonChangeSource to VideoControlsFragment::showStreamsSelection,
            FragmentVideoControlsBinding::buttonEpisodeList to VideoControlsFragment::showEpisodeSelection,
            FragmentVideoControlsBinding::buttonBack to VideoControlsFragment::switchToPipOrExit
        )
    }
}