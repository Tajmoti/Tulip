package com.tajmoti.tulip.ui.search

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tajmoti.libtulip.model.hosted.HostedItem
import com.tajmoti.libtulip.model.info.TulipSearchResult
import com.tajmoti.libtulip.model.key.ItemKey
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.tulip.BaseFragment
import com.tajmoti.tulip.R
import com.tajmoti.tulip.databinding.FragmentSearchBinding
import com.tajmoti.tulip.ui.setupWithAdapterAndDivider
import com.tajmoti.tulip.ui.show.TabbedTvShowActivity
import com.tajmoti.tulip.ui.streams.StreamsFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment : BaseFragment<FragmentSearchBinding, SearchViewModel>(
    FragmentSearchBinding::inflate
), SearchView.OnQueryTextListener {
    override val viewModel: SearchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = SearchAdapter()
        binding.viewModel = viewModel
        binding.recyclerSearch.setupWithAdapterAndDivider(adapter)
        adapter.callback = this::onSearchResultClicked
        viewModel.state.observe(viewLifecycleOwner) { onStateChanged(it, adapter) }
        viewModel.itemToOpen.observe(viewLifecycleOwner) { goToItemByKey(it!!) }
    }

    private fun onStateChanged(it: SearchViewModel.State, adapter: SearchAdapter) {
        if (it is SearchViewModel.State.Success)
            adapter.items = it.results
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
            .setItems(labels) { _, index -> onUnidentifiedItemClicked(variants[index]) }
            .setTitle(R.string.select_source)
            .setNeutralButton(R.string.back, null)
            .show()
    }

    private fun onUnidentifiedItemClicked(info: HostedItem) {
        val key = when (info) {
            is HostedItem.TvShow -> TvShowKey.Hosted(info.service, info.info.key)
            is HostedItem.Movie -> MovieKey.Hosted(info.service, info.info.key)
        }
        goToItemByKey(key)
    }

    private fun goToItemByKey(key: ItemKey) {
        when (key) {
            is TvShowKey -> {
                val intent = TabbedTvShowActivity.newInstance(requireContext(), key)
                startActivity(intent)
            }
            is MovieKey -> {
                StreamsFragment.newInstance(key)
                    .show(childFragmentManager, "streams")
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        requireActivity().menuInflater.inflate(R.menu.menu_main, menu)
        val myActionMenuItem = menu.findItem(R.id.action_search)
        val searchView = myActionMenuItem.actionView as SearchView
        searchView.setOnQueryTextListener(this)
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        return true
    }

    override fun onQueryTextChange(newText: String): Boolean {
        viewModel.submitNewText(newText)
        return true
    }
}