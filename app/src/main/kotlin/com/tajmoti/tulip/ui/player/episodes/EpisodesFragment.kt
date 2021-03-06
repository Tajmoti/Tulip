package com.tajmoti.tulip.ui.player.episodes

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.tajmoti.libtulip.dto.SeasonDto
import com.tajmoti.libtulip.dto.SeasonEpisodeDto
import com.tajmoti.libtulip.dto.TvShowSeasonDto
import com.tajmoti.libtulip.dto.EpisodeInfoDto
import com.tajmoti.libtulip.ui.player.VideoPlayerViewModel
import com.tajmoti.libtulip.ui.tvshow.TvShowViewModel
import com.tajmoti.tulip.databinding.FragmentEpisodesBinding
import com.tajmoti.tulip.ui.base.BaseFragment
import com.tajmoti.tulip.ui.getSeasonTitle
import com.tajmoti.tulip.ui.player.AndroidVideoPlayerViewModel
import com.tajmoti.tulip.ui.show.EpisodesAdapter
import com.tajmoti.tulip.ui.showEpisodeDetailsDialog
import com.tajmoti.tulip.ui.utils.activityViewModelsDelegated
import com.tajmoti.tulip.ui.utils.consume
import com.tajmoti.tulip.ui.utils.slideToBottomDismiss
import com.tajmoti.tulip.ui.utils.viewModelsDelegated
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EpisodesFragment : BaseFragment<FragmentEpisodesBinding>(FragmentEpisodesBinding::inflate) {
    private val playerViewModel by activityViewModelsDelegated<VideoPlayerViewModel, AndroidVideoPlayerViewModel>()
    private val viewModel by viewModelsDelegated<TvShowViewModel, AndroidEpisodesViewModel>()

    /**
     * Adapter for the spinner of seasons
     */
    private lateinit var seasonsAdapter: ArrayAdapter<String>

    /**
     * Adapter for the recycler of episodes
     */
    private lateinit var episodesAdapter: EpisodesAdapter


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonBack.setOnClickListener { slideToBottomDismiss() }
        seasonsAdapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item
        )
        episodesAdapter = EpisodesAdapter(::onStreamClicked, ::onEpisodeDetailsClicked)
        binding.recyclerEpisodes.adapter = episodesAdapter
        consume(viewModel.seasons) { it?.let { onSeasonsChanged(it) } }
        consume(viewModel.selectedSeason) { it?.let { onSeasonChanged(it) } }
        binding.spinnerSelectSeason.adapter = seasonsAdapter
        binding.spinnerSelectSeason.onItemSelectedListener = SpinnerListener()
    }

    private fun onStreamClicked(episode: SeasonEpisodeDto) {
        slideToBottomDismiss()
        playerViewModel.changeStreamable(episode.key)
    }

    private fun onEpisodeDetailsClicked(episode: SeasonEpisodeDto) {
        showEpisodeDetailsDialog(requireContext(), episode)
    }

    private fun onSeasonsChanged(seasons: List<TvShowSeasonDto>) {
        seasonsAdapter.clear()
        seasonsAdapter.addAll(seasons.map { getSeasonTitle(requireContext(), it) })
        preselectPlayingSeason(seasons)
    }

    private fun preselectPlayingSeason(seasons: List<TvShowSeasonDto>) {
        val episodeInfo = playerViewModel.streamableInfo.value as? EpisodeInfoDto
            ?: return
        val playingSeason = episodeInfo.seasonNumber
        val currentSeasonIndex = seasons.indexOfFirst { it.seasonNumber == playingSeason }
            .takeIf { it >= 0 }
            ?: return
        binding.spinnerSelectSeason.setSelection(currentSeasonIndex)
    }

    private fun onSeasonClicked(index: Int) {
        val item = viewModel.seasons.value?.getOrNull(index) ?: return
        viewModel.onSeasonSelected(item.key)
    }

    private fun onSeasonChanged(season: SeasonDto) {
        episodesAdapter.items = season.episodes
    }

    inner class SpinnerListener : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
            onSeasonClicked(p2)
        }

        override fun onNothingSelected(p0: AdapterView<*>?) {

        }
    }

    companion object {
        /**
         * TV show key to be used to load the episodes.
         */
        const val ARG_TV_SHOW_KEY = "itemKey"
    }
}