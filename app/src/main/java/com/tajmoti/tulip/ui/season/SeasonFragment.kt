package com.tajmoti.tulip.ui.season

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.tajmoti.libtvprovider.show.Episode
import com.tajmoti.tulip.BaseFragment
import com.tajmoti.tulip.databinding.FragmentSeasonBinding
import com.tajmoti.tulip.ui.setupWithAdapterAndDivider
import com.tajmoti.tulip.ui.streams.StreamsFragment
import dagger.hilt.android.AndroidEntryPoint
import java.io.Serializable

@AndroidEntryPoint
class SeasonFragment : BaseFragment<FragmentSeasonBinding, SeasonViewModel>(
    FragmentSeasonBinding::inflate
) {
    override val viewModel: SeasonViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = SeasonAdapter()
        adapter.callback = this::goToStreamsScreen
        binding.viewModel = viewModel
        binding.recyclerSearch.setupWithAdapterAndDivider(adapter)
        viewModel.state.observe(viewLifecycleOwner) {
            if (it is SeasonViewModel.State.Success)
                adapter.items = it.season.episodes
        }
        viewModel.fetchEpisodes(requireArguments().getSerializable(ARG_SEASON_KEY)!!)
    }

    private fun goToStreamsScreen(episode: Episode) {
        val streamableId = episode.key
        val frag = StreamsFragment.newInstance(streamableId)
        frag.show(childFragmentManager, "Streams")
    }

    companion object {
        private const val ARG_SEASON_KEY = "id"

        @JvmStatic
        fun newInstance(key: Serializable): SeasonFragment {
            val args = Bundle()
            args.putSerializable(ARG_SEASON_KEY, key)
            val fragment = SeasonFragment()
            fragment.arguments = args
            return fragment
        }
    }
}