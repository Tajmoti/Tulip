package com.tajmoti.tulip.ui.show

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.findNavController
import com.tajmoti.libtulip.model.info.TulipSeasonInfo
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.ui.tvshow.TvShowViewModel
import com.tajmoti.tulip.databinding.ActivityTabbedTvShowBinding
import com.tajmoti.tulip.ui.MainActivity
import com.tajmoti.tulip.ui.base.BaseFragment
import com.tajmoti.tulip.ui.getSeasonTitle
import com.tajmoti.tulip.ui.showEpisodeDetailsDialog
import com.tajmoti.tulip.ui.utils.consume
import com.tajmoti.tulip.ui.utils.consumeNotNull
import com.tajmoti.tulip.ui.utils.setupWithAdapterAndDivider
import com.tajmoti.tulip.ui.utils.viewModelsDelegated
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


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        setupHeader(view.context)
        binding.recyclerTvShow.setupWithAdapterAndDivider(episodesAdapter)
        consumeNotNull(viewModel.seasons, this::onSeasonsChanged)
        consume(viewModel.selectedSeason, this::onSelectedSeasonChanged)
    }

    private fun setupHeader(context: Context) {
        val binding = binding.header
        toolbar = binding.toolbar
        binding.viewModel = viewModel
        seasonsAdapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item)
        episodesAdapter = EpisodesAdapter(
            { goToStreamsScreen(it.key) },
            { showEpisodeDetailsDialog(requireContext(), it) }
        )
        binding.spinnerSelectSeason.adapter = seasonsAdapter
        binding.spinnerSelectSeason.onItemSelectedListener = SpinnerListener()
    }

    inner class SpinnerListener : OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            onSeasonClicked(position)
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {

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

    private fun onSeasonsChanged(seasons: List<TulipSeasonInfo>) {
        val seasonKey = viewModel.selectedSeason.value
        seasonsAdapter.clear()
        seasonsAdapter.addAll(seasons.map { getSeasonTitle(requireContext(), it) })
        updateSpinnerSelection(binding.header.spinnerSelectSeason, seasons, seasonKey)
    }

    private fun onSeasonClicked(index: Int) {
        val item = viewModel.seasons.value?.getOrNull(index) ?: return
        viewModel.onSeasonSelected(item.key)
    }

    private fun onSelectedSeasonChanged(season: SeasonKey?) {
        val seasons = viewModel.seasons.value
        episodesAdapter.items = seasons
            ?.firstOrNull { it.key == season }
            ?.episodes
            ?: return
        val spinner = binding.header.spinnerSelectSeason
        updateSpinnerSelection(spinner, seasons, season)
    }

    private fun updateSpinnerSelection(spinner: Spinner, seasons: List<TulipSeasonInfo>, seasonKey: SeasonKey?) {
        val oldPos = seasons
            .indexOfFirst { it.key == seasonKey }
            .takeIf { it != -1 }
            ?: return
        if (oldPos < seasons.size)
            spinner.setSelection(oldPos)
    }

    private fun goToStreamsScreen(episodeKey: EpisodeKey) {
        TvShowFragmentDirections.actionNavigationTvShowToVideoPlayerActivity(episodeKey)
            .let { findNavController().navigate(it) }
    }
}