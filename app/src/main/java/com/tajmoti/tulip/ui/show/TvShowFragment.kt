package com.tajmoti.tulip.ui.show

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.bumptech.glide.Glide
import com.tajmoti.libtulip.model.info.TulipEpisodeInfo
import com.tajmoti.libtulip.model.info.TulipSeasonInfo
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.ui.tvshow.TvShowViewModel
import com.tajmoti.tulip.databinding.ActivityTabbedTvShowBinding
import com.tajmoti.tulip.ui.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TvShowFragment : BaseFragment<ActivityTabbedTvShowBinding>(
    ActivityTabbedTvShowBinding::inflate
) {
    private val viewModel by viewModelsDelegated<TvShowViewModel, AndroidTvShowViewModel>()

    /**
     * Adapter for the spinner of seasons
     */
    private lateinit var seasonsAdapter: ArrayAdapter<String>

    /**
     * Adapter for the recycler of episodes
     */
    private lateinit var episodesAdapter: EpisodesAdapter

    /**
     * Toolbar included in the layout.
     */
    private lateinit var toolbar: Toolbar

    /**
     * Previously selected season (restore on re-creation)
     */
    private var selectedSeasonIndex: Int? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        selectedSeasonIndex = savedInstanceState
            ?.getInt(SAVED_STATE_SELECTED_SEASON, -1)
            .takeIf { it != -1 }
        binding.root.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        binding.viewModel = viewModel
        setupHeader(view.context)
        binding.recyclerTvShow.adapter = episodesAdapter
        val divider = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        binding.recyclerTvShow.addItemDecoration(divider)
        consume(viewModel.backdropPath) { it?.let { onBackdropPathChanged(it) } }
        consume(viewModel.seasons) { it?.let { onSeasonsChanged(it) } }
        consume(viewModel.name) { it?.let { onNameChanged(it) } }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val selectedSeason = binding.header.spinnerSelectSeason.selectedItemPosition
        outState.putInt(SAVED_STATE_SELECTED_SEASON, selectedSeason)
    }

    private fun setupHeader(context: Context) {
        val binding = binding.header
        toolbar = binding.toolbar
        binding.viewModel = viewModel
        binding.fab.setOnClickListener { viewModel.toggleFavorites() }
        seasonsAdapter = ArrayAdapter<String>(
            context,
            android.R.layout.simple_spinner_dropdown_item
        )
        episodesAdapter = EpisodesAdapter(
            { goToStreamsScreen(it.key) },
            { showEpisodeDetailsDialog(requireContext(), it) }
        )
        binding.spinnerSelectSeason.adapter = seasonsAdapter
        binding.spinnerSelectSeason.onItemSelectedListener = SpinnerListener()
    }

    inner class SpinnerListener : OnItemSelectedListener {
        override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
            onSeasonClicked(p2)
        }

        override fun onNothingSelected(p0: AdapterView<*>?) {

        }
    }

    override fun onStart() {
        super.onStart()
        (requireActivity() as MainActivity).swapActionBar(toolbar)
        toolbar.title = ""
    }

    override fun onStop() {
        super.onStop()
        (requireActivity() as MainActivity).swapActionBar(null)
    }

    private fun onNameChanged(name: String) {
        binding.header.title.text = name
    }

    private fun onBackdropPathChanged(backdropPath: String) {
        Glide.with(this).load(backdropPath).into(binding.header.imgTvShow)
    }

    private fun onSeasonsChanged(seasons: List<TulipSeasonInfo>) {
        val oldPos = selectedSeasonIndex ?: binding.header.spinnerSelectSeason.selectedItemPosition
        selectedSeasonIndex = null
        seasonsAdapter.clear()
        seasonsAdapter.addAll(seasons.map { getSeasonTitle(requireContext(), it) })
        if (oldPos < seasons.size)
            binding.header.spinnerSelectSeason.setSelection(oldPos)
    }

    private fun onSeasonClicked(index: Int) {
        val item = viewModel.seasons.value?.getOrNull(index) ?: return
        onSeasonChanged(item.episodes)
    }

    private fun onSeasonChanged(episodes: List<TulipEpisodeInfo>) {
        episodesAdapter.items = episodes
    }

    private fun goToStreamsScreen(episodeKey: EpisodeKey) {
        TvShowFragmentDirections.actionNavigationTvShowToVideoPlayerActivity(episodeKey)
            .let { findNavController().navigate(it) }
    }

    companion object {
        private const val SAVED_STATE_SELECTED_SEASON = "selected_season"
    }
}