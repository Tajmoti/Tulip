package com.tajmoti.tulip.ui.show

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.tajmoti.libtulip.model.StreamingService
import com.tajmoti.libtvprovider.Season
import com.tajmoti.tulip.ui.season.SeasonFragment

class SectionsPagerAdapter(
    private val seasons: List<Season>,
    fm: FragmentManager,
    private val service: StreamingService,
    private val tvShowId: String
) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        val season = seasons[position]
        return SeasonFragment.newInstance(service, tvShowId, season.key)
    }

    override fun getPageTitle(position: Int): CharSequence {
        return "Season ${seasons[position].number}"
    }

    override fun getCount(): Int {
        return seasons.size
    }
}