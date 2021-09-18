package com.tajmoti.tulip.ui.search

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tajmoti.libtulip.model.hosted.HostedItem
import com.tajmoti.libtulip.model.hosted.toKey
import com.tajmoti.libtulip.model.info.TulipSearchResult
import com.tajmoti.libtulip.model.key.ItemKey
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.ui.search.SearchViewModel
import com.tajmoti.tulip.R
import com.tajmoti.tulip.databinding.FragmentSearchBinding
import com.tajmoti.tulip.ui.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment : BaseFragment<FragmentSearchBinding, AndroidSearchViewModel>(
    FragmentSearchBinding::inflate
), SearchView.OnQueryTextListener {
    private lateinit var searchView: SearchView
    private val viewModel by viewModelsDelegated<SearchViewModel, AndroidSearchViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fixLayoutCentering()
        binding.viewModel = viewModel
        val adapter = SearchAdapter()
            .apply { callback = this@SearchFragment::onSearchResultClicked }
            .setToRecyclerWithDividers(binding.recyclerSearch)
        binding.recyclerSearch.itemAnimator = null
        consume(viewModel.results) { adapter.items = it }
        consume(viewModel.itemToOpen, this::goToItemByKey)
        consume(viewModel.status, this::setStatusView)

    }

    private fun fixLayoutCentering() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            ViewCompat.onApplyWindowInsets(
                view, insets.replaceSystemWindowInsets(
                    insets.systemWindowInsetLeft, insets.systemWindowInsetTop,
                    insets.systemWindowInsetRight, 0
                )
            )
        }
    }

    override fun onStart() {
        super.onStart()
        (requireActivity() as MainActivity).swapActionBar(binding.toolbar)
    }

    override fun onStop() {
        super.onStop()
        (requireActivity() as MainActivity).swapActionBar(null)
    }

    private fun onSearchResultClicked(result: TulipSearchResult) {
        val id = result.tmdbId
        val results = result.results
        if (id != null) {
            viewModel.onItemClicked(id)
        } else {
            onUnidentifiedResultClicked(results)
        }
    }

    private fun onUnidentifiedResultClicked(variants: List<HostedItem>) {
        val labels = variants
            .map { "[${it.info.language.uppercase()}][${it.service}] ${it.info.name}" }
            .toTypedArray()
        MaterialAlertDialogBuilder(requireContext())
            .setItems(labels) { _, index -> goToItemByKey(variants[index].toKey()) }
            .setTitle(R.string.other_results)
            .setNeutralButton(R.string.back, null)
            .show()
    }

    private fun goToItemByKey(key: ItemKey) {
        searchView.clearFocus()
        val navController = findNavController()
        when (key) {
            is TvShowKey -> {
                SearchFragmentDirections.actionNavigationSearchToTabbedTvShowActivity(key)
                    .let { navController.navigate(it) }
            }
            is MovieKey -> {
                SearchFragmentDirections.actionNavigationSearchToVideoPlayerActivity(key)
                    .let { navController.navigate(it) }
            }
        }
    }

    private fun setStatusView(it: SearchViewModel.Icon?) {
        val textToImage = when (it) {
            SearchViewModel.Icon.READY -> R.string.search_hint to R.drawable.ic_search_24
            SearchViewModel.Icon.NO_RESULTS -> R.string.no_results to R.drawable.ic_sad_24
            SearchViewModel.Icon.ERROR -> R.string.something_went_wrong to R.drawable.ic_sad_24
            else -> null
        }
        if (textToImage == null) {
            binding.statusText.isVisible = false
            binding.statusImage.isVisible = false
        } else {
            binding.statusText.isVisible = true
            binding.statusImage.isVisible = true
            val (text, image) = textToImage
            binding.statusText.setText(text)
            binding.statusImage.setImageResource(image)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        requireActivity().menuInflater.inflate(R.menu.menu_main, menu)
        val item = menu.findItem(R.id.action_search)
        searchView = (item.actionView as SearchView)
            .apply { setOnQueryTextListener(this@SearchFragment) }
            .apply { queryHint = getString(R.string.search_query_hint) }
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        return true
    }

    override fun onQueryTextChange(newText: String): Boolean {
        viewModel.submitNewText(newText)
        return true
    }
}