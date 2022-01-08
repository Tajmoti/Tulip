package com.tajmoti.tulip.ui.show

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.tajmoti.libtulip.model.info.TulipEpisodeInfo
import com.tajmoti.libtulip.model.info.TulipSeasonInfo
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.ui.tvshow.TvShowViewModel
import com.tajmoti.tulip.databinding.ActivityTabbedTvShowBinding
import com.tajmoti.tulip.databinding.LayoutTvShowHeaderBinding
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
     * TV show info header. Inflated when RecyclerView requests it.
     */
    private var header: LayoutTvShowHeaderBinding? = null

    /**
     * Padding to be applied to the toolbar included in the header item.
     */
    private var topPadding = 0


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        seasonsAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item)
        episodesAdapter = EpisodesAdapter(
            this::goToStreamsScreen,
            this::showEpisodeDetailsDialog,
            this::inflateAndSetupHeader
        )
        binding.recyclerTvShow.setupWithAdapterAndDivider(episodesAdapter)
        consume(viewModel.seasons, this::onSeasonsChanged)
        consume(viewModel.selectedSeason, this::onSelectedSeasonChanged)

        binding.recyclerTvShow.setOnApplyWindowInsetsListener { _, insets ->
            topPadding = insets.getInsets(WindowInsets.Type.systemBars()).top
            insets
        }
    }

    private fun inflateAndSetupHeader(inflater: LayoutInflater, root: ViewGroup, attach: Boolean): ViewBinding {
        val binding = LayoutTvShowHeaderBinding.inflate(inflater, root, attach)
        binding.lifecycleOwner = viewLifecycleOwner
        header = binding
        binding.viewModel = viewModel
        binding.toolbar.updatePadding(top = topPadding)
        binding.spinnerSelectSeason.adapter = seasonsAdapter
        binding.spinnerSelectSeason.onItemSelectedListener = SpinnerListener()
        (requireActivity() as MainActivity).swapActionBar(binding.toolbar)
        binding.toolbar.title = ""
        viewModel.seasons.value?.let(this::onSeasonsChanged)
        return binding
    }

    inner class SpinnerListener : OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            onSeasonClicked(position)
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {

        }
    }

    override fun onStop() {
        super.onStop()
        (requireActivity() as MainActivity).swapActionBar(null)
    }

    private fun onSeasonsChanged(seasons: List<TulipSeasonInfo>?) {
        seasons ?: return
        val header = header ?: return
        val seasonKey = viewModel.selectedSeason.value
        seasonsAdapter.clear()
        seasonsAdapter.addAll(seasons.map { getSeasonTitle(requireContext(), it) })
        updateSpinnerSelection(header.spinnerSelectSeason, seasons, seasonKey)
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
        val spinner = header?.spinnerSelectSeason ?: return
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

    private fun goToStreamsScreen(episode: TulipEpisodeInfo) {
        TvShowFragmentDirections.actionNavigationTvShowToVideoPlayerActivity(episode.key)
            .let { findNavController().navigate(it) }
    }

    private fun showEpisodeDetailsDialog(episode: TulipEpisodeInfo) {
        showEpisodeDetailsDialog(requireContext(), episode)
    }
}