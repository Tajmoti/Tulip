package com.tajmoti.tulip.ui.search

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import com.tajmoti.libtvprovider.TvItem
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
        adapter.callback = this::goToTvShowScreen
        viewModel.state.observe(viewLifecycleOwner) { onStateChanged(it, adapter) }
    }

    private fun onStateChanged(it: SearchViewModel.State, adapter: SearchAdapter) {
        if (it is SearchViewModel.State.Success)
            adapter.items = it.items
    }

    private fun goToTvShowScreen(it: TvItem) {
        if (it is TvItem.Show) {
            val intent = Intent(requireContext(), TabbedTvShowActivity::class.java)
                .putExtra(TabbedTvShowActivity.ARG_TV_SHOW_ID, it.key)
            startActivity(intent)
        } else {
            StreamsFragment.newInstance(it.key)
                .show(childFragmentManager, "streams")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        requireActivity().menuInflater.inflate(R.menu.menu_main, menu)
        val myActionMenuItem: MenuItem = menu.findItem(R.id.action_search)
        val searchView: SearchView = myActionMenuItem.actionView as SearchView
        searchView.setOnQueryTextListener(this)
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        viewModel.search(query)
        return true
    }

    override fun onQueryTextChange(newText: String): Boolean {
        return false
    }
}