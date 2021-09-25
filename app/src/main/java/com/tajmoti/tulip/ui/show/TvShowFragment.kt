package com.tajmoti.tulip.ui.show

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.bumptech.glide.Glide
import com.tajmoti.libtulip.model.info.TulipSeasonInfo
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.ui.tvshow.TvShowViewModel
import com.tajmoti.tulip.R
import com.tajmoti.tulip.databinding.ActivityTabbedTvShowBinding
import com.tajmoti.tulip.ui.BaseFragment
import com.tajmoti.tulip.ui.MainActivity
import com.tajmoti.tulip.ui.consume
import com.tajmoti.tulip.ui.viewModelsDelegated
import com.xwray.groupie.ExpandableGroup
import com.xwray.groupie.GroupieAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class TvShowFragment : BaseFragment<ActivityTabbedTvShowBinding>(
    ActivityTabbedTvShowBinding::inflate
) {
    private val viewModel by viewModelsDelegated<TvShowViewModel, AndroidTvShowViewModel>()
    private lateinit var adapter: GroupieAdapter

    /**
     * Seasons which are already set to the UI.
     * Avoids useless Groupie adapter refreshes since they don't work properly.
     */
    private var appliedSeasons: List<TulipSeasonInfo>? = null

    /**
     * List of expanded season indices to restore when recreating the fragment.
     * Cleared after the list is loaded the first time.
     */
    private var expandedIndices: List<Int>? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        expandedIndices = savedInstanceState?.getIntArray(SAVED_STATE_EXPANDED_SEASONS)?.toList()

        binding.viewModel = viewModel
        binding.fab.setOnClickListener { viewModel.toggleFavorites() }
        adapter = GroupieAdapter()
        binding.recyclerTvShow.adapter = adapter
        val divider = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        binding.recyclerTvShow.addItemDecoration(divider)

        consume(viewModel.backdropPath) { it?.let { onBackdropPathChanged(it) } }
        consume(viewModel.seasons) { it?.let { onSeasonsChanged(it) } }
        consume(viewModel.name) { it?.let { onNameChanged(it) } }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val indices = getCurrentlyExpandedIndices().toIntArray()
        outState.putIntArray(SAVED_STATE_EXPANDED_SEASONS, indices)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        appliedSeasons = null
    }

    override fun onStart() {
        super.onStart()
        (requireActivity() as MainActivity).swapActionBar(binding.toolbar)
    }

    override fun onStop() {
        super.onStop()
        (requireActivity() as MainActivity).swapActionBar(null)
    }

    private fun onNameChanged(name: String) {
        binding.toolbarLayout.title = name
    }

    private fun onBackdropPathChanged(backdropPath: String) {
        Glide.with(this).load(backdropPath).into(binding.imgTvShow)
    }

    private suspend fun onSeasonsChanged(seasons: List<TulipSeasonInfo>) {
        if (appliedSeasons == seasons)
            return
        appliedSeasons = seasons
        val seasonGroups = withContext(Dispatchers.Default) {
            seasons.map { season -> createEpisodeGroup(season) }
        }
        updateAndRestoreExpandState(seasonGroups)
    }

    private fun updateAndRestoreExpandState(seasonGroups: List<ExpandableGroup>) {
        // Remember expanded positions
        val expandedIndicesLocal = getInitiallyExpandedIndices() ?: getCurrentlyExpandedIndices()
        adapter.replaceAll(seasonGroups)
        // Re-expand them and ignore potential errors (very unlikely)
        expandedIndicesLocal
            .forEach { runCatching { getExpandableAtPos(it).onToggleExpanded() } }
    }

    private fun getInitiallyExpandedIndices(): List<Int>? {
        return expandedIndices?.also { expandedIndices = null }
    }

    private fun getCurrentlyExpandedIndices(): List<Int> {
        return (0 until adapter.groupCount)
            .filter { getExpandableAtPos(it).isExpanded }
    }

    private fun getExpandableAtPos(it: Int): ExpandableGroup {
        val group = adapter.getTopLevelGroup(it)
        return group as ExpandableGroup
    }

    private fun createEpisodeGroup(season: TulipSeasonInfo): ExpandableGroup {
        val seasonTitle = if (season.seasonNumber == 0) {
            getString(R.string.season_specials)
        } else {
            getString(R.string.season_no, season.seasonNumber)
        }
        val header = ExpandableHeaderItem(seasonTitle)
        val groupAdapter = ExpandableGroup(header)
        val mapped = season.episodes
            .map { EpisodeItem(it.first.number, it.first.name) { goToStreamsScreen(it.second) } }
        groupAdapter.addAll(mapped)
        return groupAdapter
    }

    private fun goToStreamsScreen(episodeKey: EpisodeKey) {
        TvShowFragmentDirections.actionNavigationTvShowToVideoPlayerActivity(episodeKey)
            .let { findNavController().navigate(it) }
    }

    companion object {
        private const val SAVED_STATE_EXPANDED_SEASONS = "expanded_seasons"
    }
}