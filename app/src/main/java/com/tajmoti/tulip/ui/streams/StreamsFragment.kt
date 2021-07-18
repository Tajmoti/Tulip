package com.tajmoti.tulip.ui.streams

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.tajmoti.tulip.BaseFragment
import com.tajmoti.tulip.databinding.FragmentStreamsBinding
import com.tajmoti.tulip.ui.setupWithAdapterAndDivider
import dagger.hilt.android.AndroidEntryPoint
import java.io.Serializable

@AndroidEntryPoint
class StreamsFragment : BaseFragment<FragmentStreamsBinding, StreamsViewModel>(
    FragmentStreamsBinding::inflate
) {
    override val viewModel: StreamsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = StreamsAdapter()
        adapter.callback = this::onStreamClicked
        binding.viewModel = viewModel
        binding.recyclerSearch.setupWithAdapterAndDivider(adapter)
        viewModel.streamLoadingState.observe(viewLifecycleOwner) {
            onStreamLoadingStateChanged(
                it,
                adapter
            )
        }
        viewModel.directStreamLoadingState.observe(viewLifecycleOwner) { onDirectLoadingChanged(it) }
        viewModel.fetchStreams(requireArguments().getSerializable(ARG_STREAMABLE_ID)!!)
    }

    private fun onStreamLoadingStateChanged(it: StreamsViewModel.State, adapter: StreamsAdapter) {
        if (it is StreamsViewModel.State.Success)
            adapter.items = it.streams
    }

    private fun onDirectLoadingChanged(it: StreamsViewModel.DirectStreamLoading?) {
        it ?: return
        if (it is StreamsViewModel.DirectStreamLoading.Success) {
            startVideo(it.directLink, true)
        } else if (it is StreamsViewModel.DirectStreamLoading.Failed) {
            startVideo(it.video.url, false)
        }
    }

    private fun onStreamClicked(stream: UnloadedVideoStreamRef) {
        if (stream.linkExtractionSupported) {
            viewModel.fetchStreamDirect(stream.info)
        } else {
            startVideo(stream.info.url, false)
        }
    }

    private fun startVideo(url: String, direct: Boolean) {
        val intent = if (direct) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.setDataAndType(Uri.parse(url), "video/mp4")
        } else {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setData(Uri.parse(url))
        }
        startActivity(intent)
    }

    companion object {
        private const val ARG_STREAMABLE_ID = "id"

        @JvmStatic
        fun newInstance(key: Serializable): StreamsFragment {
            val args = Bundle()
            args.putSerializable(ARG_STREAMABLE_ID, key)
            val fragment = StreamsFragment()
            fragment.arguments = args
            return fragment
        }
    }
}