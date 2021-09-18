package com.tajmoti.tulip.ui.streams

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tajmoti.libtulip.model.stream.UnloadedVideoWithLanguage
import com.tajmoti.libtulip.ui.streams.FailedLink
import com.tajmoti.libtulip.ui.streams.LoadedLink
import com.tajmoti.libtulip.ui.streams.SelectedLink
import com.tajmoti.libtulip.ui.streams.StreamsViewModel
import com.tajmoti.tulip.R
import com.tajmoti.tulip.databinding.FragmentStreamsBinding
import com.tajmoti.tulip.ui.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StreamsFragment : BaseFragment<FragmentStreamsBinding, AndroidStreamsViewModel>(
    FragmentStreamsBinding::inflate
) {
    private val args: StreamsFragmentArgs by navArgs()
    private val viewModel by viewModelsDelegated<StreamsViewModel, AndroidStreamsViewModel>()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = StreamsAdapter(this::onStreamClickedDownload)
        adapter.callback = this::onStreamClickedPlay
        binding.viewModel = viewModel
        binding.recyclerSearch.setupWithAdapterAndDivider(adapter)
        consume(viewModel.linksResult) { it?.let { adapter.items = it.streams } }
        consume(viewModel.directLoadingUnsupported, this::onDirectLinkUnsupported)
        consume(viewModel.directLoaded, this::onDirectLinkLoaded)
        consume(viewModel.linkLoadingError, this::onDirectLinkLoadingError)
    }

    private fun onDirectLinkLoaded(it: LoadedLink) {
        if (!it.download) {
            startVideo(it.directLink, true)
        } else {
            toast(R.string.starting_download)
        }
    }

    private fun onDirectLinkUnsupported(it: SelectedLink) {
        if (!it.download) {
            startVideo(it.stream.url, false)
        } else {
            Toast.makeText(requireContext(), R.string.stream_not_downloadable, Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun onDirectLinkLoadingError(link: FailedLink) {
        if (link.download) {
            Toast.makeText(requireContext(), R.string.direct_loading_failure, Toast.LENGTH_SHORT)
                .show()
        } else {
            MaterialAlertDialogBuilder(requireContext())
                .setIcon(R.drawable.ic_sad_24)
                .setTitle(R.string.direct_loading_failure)
                .setMessage(R.string.direct_loading_failure_message)
                .setPositiveButton(R.string.direct_loading_failure_yes) { _, _ ->
                    startVideo(link.stream.url, false)
                }
                .setNegativeButton(R.string.direct_loading_failure_no) { _, _ -> }
                .show()
        }
    }

    private fun onStreamClickedPlay(stream: UnloadedVideoWithLanguage) {
        viewModel.onStreamClicked(stream.video, false)
    }

    private fun onStreamClickedDownload(stream: UnloadedVideoWithLanguage) {
        viewModel.onStreamClicked(stream.video, true)
    }

    private fun startVideo(url: String, direct: Boolean) {
        if (direct) {
            StreamsFragmentDirections
                .actionNavigationStreamsToVideoPlayerActivity(url, args.streamableKey)
                .let { findNavController().navigate(it) }
        } else {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                toast(R.string.web_browser_not_installed)
            }
        }
    }
}