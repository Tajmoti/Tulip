package com.tajmoti.tulip.ui.show

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
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
class TvShowFragment : BaseFragment<ActivityTabbedTvShowBinding, AndroidTvShowViewModel>(
    ActivityTabbedTvShowBinding::inflate
) {
    private val viewModel by viewModelsDelegated<TvShowViewModel, AndroidTvShowViewModel>()
    private val args: TvShowFragmentArgs by navArgs()
    private lateinit var adapter: GroupieAdapter


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        binding.fab.setOnClickListener { viewModel.toggleFavorites(args.itemKey) }
        adapter = GroupieAdapter()
        binding.recyclerTvShow.adapter = adapter
        val divider = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        binding.recyclerTvShow.addItemDecoration(divider)

        consume(viewModel.state) { onStateChanged(it) }
        consume(viewModel.name) { onNameChanged(it) }
    }

    override fun onStart() {
        super.onStart()
        (requireActivity() as MainActivity).swapActionBar(binding.toolbar)
    }

    override fun onStop() {
        super.onStop()
        (requireActivity() as MainActivity).swapActionBar(null)
    }

    private suspend fun onStateChanged(state: TvShowViewModel.State) {
        if (state !is TvShowViewModel.State.Success)
            return
        onLoadingFinished(state)
    }

    private fun onNameChanged(name: String?) {
        if (name == null)
            return
        binding.toolbarLayout.title = name
    }

    private suspend fun onLoadingFinished(state: TvShowViewModel.State.Success) {
        Glide.with(this).load(state.backdropPath).into(binding.imgTvShow)
        val seasons = withContext(Dispatchers.Default) {
            state.seasons.map { season -> createEpisodeGroup(season) }
        }
        adapter.update(seasons)
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
}