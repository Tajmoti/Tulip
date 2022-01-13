package com.tajmoti.tulip.ui.downloads

import androidx.fragment.app.viewModels
import com.tajmoti.tulip.databinding.FragmentDownloadsBinding
import com.tajmoti.tulip.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DownloadsFragment : BaseFragment<FragmentDownloadsBinding>(
    FragmentDownloadsBinding::inflate
) {
    val viewModelUndelegated: DownloadsViewModel by viewModels()
}