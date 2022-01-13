package com.tajmoti.tulip.ui.utils

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import com.tajmoti.tulip.R

fun Fragment.slideToBottomDismiss(fm: FragmentManager = requireActivity().supportFragmentManager) {
    fm.commit {
        setCustomAnimations(
            R.anim.slide_from_top_enter,
            R.anim.slide_from_top_exit,
            R.anim.slide_from_top_enter,
            R.anim.slide_from_top_exit
        )
        remove(this@slideToBottomDismiss)
    }
}
