package com.tajmoti.tulip.ui.season

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.tajmoti.libtvprovider.Episode
import com.tajmoti.tulip.BaseFragment
import com.tajmoti.tulip.databinding.FragmentSeasonBinding
import com.tajmoti.tulip.model.StreamingService
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
                adapter.items = it.season.episodes
        }
    }

    private fun startEpFetching() {
        val args = requireArguments()
        val service = args.getSerializable(ARG_SERVICE) as StreamingService
        val tvShowKey = args.getString(ARG_TV_SHOW_KEY)!!
        val seasonKey = args.getString(ARG_SEASON_KEY)!!
        viewModel.fetchEpisodes(service, tvShowKey, seasonKey)
    }

    private fun goToStreamsScreen(episode: Episode) {
        val args = requireArguments()
        val service = args.getSerializable(ARG_SERVICE) as StreamingService
        val tvShowKey = args.getString(ARG_TV_SHOW_KEY)!!
        val seasonKey = args.getString(ARG_SEASON_KEY)!!
        val streamableId = episode.key
        val frag = StreamsFragment.newInstance(service, tvShowKey, seasonKey, streamableId)
        frag.show(childFragmentManager, "Streams")
    }

    companion object {
        private const val ARG_SERVICE = "service"
        private const val ARG_TV_SHOW_KEY = "tv_show"
        private const val ARG_SEASON_KEY = "season"

        @JvmStatic
        fun newInstance(service: StreamingService, tvShow: String, season: String): SeasonFragment {
            val args = Bundle()
            args.putSerializable(ARG_SERVICE, service)
            args.putSerializable(ARG_TV_SHOW_KEY, tvShow)
            args.putSerializable(ARG_SEASON_KEY, season)
            val fragment = SeasonFragment()
            fragment.arguments = args
            return fragment
        }
    }
}