package com.tajmoti.tulip.ui.search

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tajmoti.libtulip.model.TmdbId
import com.tajmoti.libtulip.model.TulipSearchResult
import com.tajmoti.libtulip.model.TulipTvShow
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
    }

    private fun onStateChanged(it: SearchViewModel.State, adapter: SearchAdapter) {
        if (it is SearchViewModel.State.Success)
            adapter.items = it.items.groupBy { it.tmdbId }.toList()
    }

    private fun onSearchResultClicked(result: Pair<TmdbId?, List<TulipSearchResult>>) {
        val variants = result.second
        if (variants.size == 1) {
            goToTvShowScreen(variants[0])
            return
        }
        val labels = variants
            .map { "[${it.language.uppercase()}][${it.service}] ${it.name}" }
            .toTypedArray()
        MaterialAlertDialogBuilder(requireContext())
            .setItems(labels) { _, index -> goToTvShowScreen(variants[index]) }
            .setTitle(R.string.select_source)
            .setNeutralButton(R.string.back, null)
            .show()
    }

    private fun goToTvShowScreen(it: TulipSearchResult) {
        if (it is TulipTvShow) {
            val intent = Intent(requireContext(), TabbedTvShowActivity::class.java)
                .putExtra(TabbedTvShowActivity.ARG_SERVICE, it.service)
                .putExtra(TabbedTvShowActivity.ARG_TV_SHOW_ID, it.key)
            startActivity(intent)
        } else {
            StreamsFragment.newInstance(it.service, it.key)
                .show(childFragmentManager, "streams")
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