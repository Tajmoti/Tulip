package com.tajmoti.tulip.ui.show

import android.os.Bundle
import androidx.activity.viewModels
import com.tajmoti.tulip.BaseActivity
import com.tajmoti.tulip.databinding.ActivityTabbedTvShowBinding
import com.tajmoti.tulip.model.StreamingService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TabbedTvShowActivity : BaseActivity<ActivityTabbedTvShowBinding>(
    ActivityTabbedTvShowBinding::inflate
) {
    private val viewModel: TvShowViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.tabs.setupWithViewPager(binding.viewPager)
        viewModel.name.observe(this, this::onNameChanged)
        viewModel.state.observe(this, this::onStateChanged)
        val tvShowId = intent.getStringExtra(ARG_TV_SHOW_ID)!!
        val service = intent.getSerializableExtra(ARG_SERVICE) as StreamingService
        viewModel.fetchEpisodes(service, tvShowId)
    }

    private fun onStateChanged(state: TvShowViewModel.State) {
        if (state !is TvShowViewModel.State.Success)
            return
        onLoadingFinished(state)
    }

    private fun onNameChanged(name: String?) {
        if (name == null)
            return
        title = name
    }

    private fun onLoadingFinished(state: TvShowViewModel.State.Success) {
        val service = intent.getSerializableExtra(ARG_SERVICE) as StreamingService
        val tvShowId = intent.getStringExtra(ARG_TV_SHOW_ID)!!
        val sectionsPagerAdapter = SectionsPagerAdapter(
            state.items.second,
            supportFragmentManager,
            service,
            tvShowId
        )
        binding.viewPager.adapter = sectionsPagerAdapter
    }

    companion object {
        const val ARG_SERVICE = "service"
        const val ARG_TV_SHOW_ID = "tv_show_id"
    }
}