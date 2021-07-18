package com.tajmoti.tulip.ui.library

import androidx.fragment.app.viewModels
import com.tajmoti.tulip.BaseFragment
import com.tajmoti.tulip.databinding.FragmentLibraryBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LibraryFragment : BaseFragment<FragmentLibraryBinding, LibraryViewModel>(
    FragmentLibraryBinding::inflate
) {
    override val viewModel: LibraryViewModel by viewModels()
}