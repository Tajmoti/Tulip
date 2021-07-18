package com.tajmoti.tulip.ui.show

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.tajmoti.tulip.BaseActivity
import com.tajmoti.tulip.databinding.ActivityTabbedTvShowBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TabbedTvShowActivity : BaseActivity<ActivityTabbedTvShowBinding>() {
    private val viewModel: TvShowViewModel by viewModels()
    override val bindingInflater: (LayoutInflater) -> ActivityTabbedTvShowBinding =
        ActivityTabbedTvShowBinding::inflate


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tvShowId = intent.getSerializableExtra(ARG_TV_SHOW_ID)!!
        viewModel.state.observe(this, this::onStateChanged)
        viewModel.fetchEpisodes(tvShowId)
    }

    private fun onStateChanged(state: TvShowViewModel.State) {
        if (state !is TvShowViewModel.State.Success)
            return
        onLoadingFinished(state)
    }

    private fun onLoadingFinished(state: TvShowViewModel.State.Success) {
        val result = state.items
        title = result.first.name
        val sectionsPagerAdapter = SectionsPagerAdapter(result.second, supportFragmentManager)
        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = binding.tabs
        tabs.setupWithViewPager(viewPager)
    }

    companion object {
        const val ARG_TV_SHOW_ID = "tv_show_id"
    }
}