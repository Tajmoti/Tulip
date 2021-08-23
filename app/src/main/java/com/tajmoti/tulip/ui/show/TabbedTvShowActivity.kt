package com.tajmoti.tulip.ui.show

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.tulip.BaseActivity
import com.tajmoti.tulip.R
import com.tajmoti.tulip.databinding.ActivityTabbedTvShowBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TabbedTvShowActivity : BaseActivity<ActivityTabbedTvShowBinding>(
    R.layout.activity_tabbed_tv_show
) {
    private val viewModel: TvShowViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.viewModel = viewModel
        binding.tabs.setupWithViewPager(binding.viewPager)
        viewModel.name.observe(this, this::onNameChanged)
        viewModel.state.observe(this, this::onStateChanged)
        val tvShowKey = intent.getSerializableExtra(ARG_TV_SHOW_KEY) as TvShowKey
        viewModel.fetchTvShowData(tvShowKey)
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
        val sectionsPagerAdapter = SectionsPagerAdapter(
            this,
            supportFragmentManager,
            state.seasons
        )
        binding.viewPager.adapter = sectionsPagerAdapter
    }

    companion object {
        private const val ARG_TV_SHOW_KEY = "tv_show_key"

        fun newInstance(context: Context, key: TvShowKey): Intent {
            return Intent(context, TabbedTvShowActivity::class.java)
                .putExtra(ARG_TV_SHOW_KEY, key)
        }
    }
}