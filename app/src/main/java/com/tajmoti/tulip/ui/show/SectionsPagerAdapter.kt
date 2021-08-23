package com.tajmoti.tulip.ui.show

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.tulip.R
import com.tajmoti.tulip.ui.season.SeasonFragment

class SectionsPagerAdapter(
    private val context: Context,
    fm: FragmentManager,
    private val seasons: List<SeasonKey>,
) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        val season = seasons[position]
        return SeasonFragment.newInstance(season)
    }

    override fun getPageTitle(position: Int): CharSequence {
        val number = seasons[position].seasonNumber
        return if (number == 0) {
            context.getString(R.string.season_specials)
        } else {
            context.getString(R.string.season_no, number)
        }
    }

    override fun getCount(): Int {
        return seasons.size
    }
}