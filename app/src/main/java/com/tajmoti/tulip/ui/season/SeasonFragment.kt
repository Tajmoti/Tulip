package com.tajmoti.tulip.ui.season

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.tajmoti.libtulip.model.info.EpisodeInfoWithKey
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.tulip.BaseFragment
import com.tajmoti.tulip.databinding.FragmentSeasonBinding
import com.tajmoti.tulip.ui.setupWithAdapterAndDivider
import com.tajmoti.tulip.ui.streams.StreamsFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SeasonFragment : BaseFragment<FragmentSeasonBinding, SeasonViewModel>(
    FragmentSeasonBinding::inflate
) {
    override val viewModel: SeasonViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startEpFetching()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = SeasonAdapter()
        adapter.callback = this::goToStreamsScreen
        binding.viewModel = viewModel
        binding.recyclerSearch.setupWithAdapterAndDivider(adapter)
        viewModel.state.observe(viewLifecycleOwner) {
            if (it is SeasonViewModel.State.Success)
                adapter.items = it.episodes
        }
    }

    private fun startEpFetching() {
        val args = requireArguments()
        val seasonKey = args.getSerializable(ARG_SEASON_KEY) as SeasonKey
        viewModel.fetchEpisodes(seasonKey)
    }

    private fun goToStreamsScreen(episodeKey: EpisodeInfoWithKey) {
        val frag = StreamsFragment.newInstance(episodeKey.second)
        frag.show(childFragmentManager, "Streams")
    }

    companion object {
        private const val ARG_SEASON_KEY = "season"

        @JvmStatic
        fun newInstance(tvShowKey: SeasonKey): SeasonFragment {
            val args = Bundle()
            args.putSerializable(ARG_SEASON_KEY, tvShowKey)
            val fragment = SeasonFragment()
            fragment.arguments = args
            return fragment
        }
    }
}