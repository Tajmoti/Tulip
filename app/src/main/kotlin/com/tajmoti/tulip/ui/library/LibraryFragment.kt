package com.tajmoti.tulip.ui.library

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.tajmoti.libtulip.model.key.ItemKey
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.ui.library.LibraryItem
import com.tajmoti.libtulip.ui.library.LibraryViewModel
import com.tajmoti.tulip.R
import com.tajmoti.tulip.databinding.FragmentLibraryBinding
import com.tajmoti.tulip.ui.base.BaseFragment
import com.tajmoti.tulip.ui.utils.consume
import com.tajmoti.tulip.ui.utils.viewModelsDelegated
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LibraryFragment : BaseFragment<FragmentLibraryBinding>(
    FragmentLibraryBinding::inflate
) {
    private val viewModel by viewModelsDelegated<LibraryViewModel, AndroidLibraryViewModel>()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel

        val columns = resources.getInteger(R.integer.library_columns)
        val manager = GridLayoutManager(requireContext(), columns)
        val adapter = LibraryAdapter(this::onItemImageClick, this::onDetailsClicked)
        binding.recyclerFavorites.layoutManager = manager
        binding.recyclerFavorites.adapter = adapter
        consume(viewModel.favoriteItems) { adapter.items = it }
    }

    private fun onDetailsClicked(key: ItemKey) {
        val navController = findNavController()
        when (key) {
            is TvShowKey -> {
                LibraryFragmentDirections.actionNavigationLibraryToTabbedTvShowActivity(key)
                    .let { navController.navigate(it) }
            }
            is MovieKey -> {
                LibraryFragmentDirections.actionNavigationLibraryToVideoPlayerActivity(key)
                    .let { navController.navigate(it) }
            }
        }
    }

    private fun onItemImageClick(item: LibraryItem) {
        val pos = item.lastPlayedPosition
        if (pos == null) {
            onDetailsClicked(item.key)
            return
        }
        val key = pos.key
        LibraryFragmentDirections.actionNavigationLibraryToVideoPlayerActivity(key)
            .let { findNavController().navigate(it) }
    }
}