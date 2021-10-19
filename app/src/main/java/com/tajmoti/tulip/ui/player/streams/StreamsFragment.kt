package com.tajmoti.tulip.ui.player.streams

import android.os.Bundle
import android.view.View
import com.tajmoti.libtulip.model.info.StreamableInfo
import com.tajmoti.libtulip.model.info.TulipCompleteEpisodeInfo
import com.tajmoti.libtulip.model.info.TulipMovie
import com.tajmoti.libtulip.model.stream.UnloadedVideoWithLanguage
import com.tajmoti.libtulip.ui.player.VideoPlayerViewModel
import com.tajmoti.tulip.databinding.FragmentStreamsBinding
import com.tajmoti.tulip.ui.*
import com.tajmoti.tulip.ui.player.AndroidVideoPlayerViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StreamsFragment : BaseFragment<FragmentStreamsBinding>(FragmentStreamsBinding::inflate) {
    private val viewModel by activityViewModelsDelegated<VideoPlayerViewModel, AndroidVideoPlayerViewModel>()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = StreamsAdapter(this::onStreamClickedPlay, this::onStreamClickedDownload)
        binding.recyclerSearch.setupWithAdapterAndDivider(adapter)
        binding.buttonBack.setOnClickListener { slideToBottomDismiss() }
        consume(viewModel.linksResult) { it?.let { adapter.items = it.streams } }
        consume(viewModel.streamableInfo, this::onStreamableInfo)
    }

    /**
     * Info about the streamable is known, show its name in the UI.
     */
    private fun onStreamableInfo(info: StreamableInfo?) {
        val name = when (info) {
            is TulipCompleteEpisodeInfo -> showToDisplayName(info)
            is TulipMovie -> info.name
            null -> ""
        }
        binding.titleStreamSelection.text = name
    }


    /**
     * A video link was clicked, load it and play it.
     */
    private fun onStreamClickedPlay(stream: UnloadedVideoWithLanguage) {
        slideToBottomDismiss()
        viewModel.onStreamClicked(stream.video, false)
    }

    /**
     * A video link was long-clicked which means download.
     */
    private fun onStreamClickedDownload(stream: UnloadedVideoWithLanguage) {
        viewModel.onStreamClicked(stream.video, true)
    }
}