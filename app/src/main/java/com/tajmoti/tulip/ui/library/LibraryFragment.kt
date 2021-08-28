package com.tajmoti.tulip.ui.library

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.tajmoti.libtulip.model.info.TulipItemInfo
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.tulip.databinding.FragmentLibraryBinding
import com.tajmoti.tulip.ui.BaseFragment
import com.tajmoti.tulip.ui.consume
import com.tajmoti.tulip.ui.streams.StreamsFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class LibraryFragment : BaseFragment<FragmentLibraryBinding, LibraryViewModel>(
    FragmentLibraryBinding::inflate
) {
    override val viewModel: LibraryViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel

        val manager = GridLayoutManager(requireContext(), 2)
        val adapter = FavoritesAdapter()
        binding.recyclerFavorites.layoutManager = manager
        binding.recyclerFavorites.adapter = adapter
        adapter.callback = {
            startItem(it)
        }

        consume { viewModel.favoriteItems.collect { adapter.items = it } }
    }

    private fun startItem(it: TulipItemInfo) {
        when (val key = it.key) {
            is TvShowKey -> {
                val action =
                    LibraryFragmentDirections.actionNavigationLibraryToTabbedTvShowActivity(key)
                findNavController().navigate(action)
            }
            is MovieKey -> {
                StreamsFragment.newInstance(key)
                    .show(childFragmentManager, "streams")
            }
        }
    }
}