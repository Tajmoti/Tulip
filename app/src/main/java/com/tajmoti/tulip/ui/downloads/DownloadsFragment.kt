package com.tajmoti.tulip.ui.downloads

import androidx.fragment.app.viewModels
import com.tajmoti.tulip.databinding.FragmentDownloadsBinding
import com.tajmoti.tulip.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DownloadsFragment : BaseFragment<FragmentDownloadsBinding, DownloadsViewModel>(
    FragmentDownloadsBinding::inflate
) {
    override val viewModel: DownloadsViewModel by viewModels()
}