package com.tajmoti.tulip.ui.streams

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.StreamableKey
import com.tajmoti.libtulip.model.stream.FinalizedVideoInformation
import com.tajmoti.libtulip.model.stream.UnloadedVideoWithLanguage
import com.tajmoti.tulip.R
import com.tajmoti.tulip.databinding.FragmentStreamsBinding
import com.tajmoti.tulip.ui.BaseFragment
import com.tajmoti.tulip.ui.setupWithAdapterAndDivider
import com.tajmoti.tulip.ui.toast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StreamsFragment : BaseFragment<FragmentStreamsBinding, StreamsViewModel>(
    FragmentStreamsBinding::inflate
) {
    override val viewModel: StreamsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val streamInfo = getStreamInfo()
        viewModel.fetchStreamsWithLanguages(streamInfo)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = StreamsAdapter(this::onStreamClickedDownload)
        adapter.callback = this::onStreamClickedPlay
        binding.viewModel = viewModel
        binding.recyclerSearch.setupWithAdapterAndDivider(adapter)
        viewModel.streamLoadingState.observe(viewLifecycleOwner) {
            onStreamLoadingStateChanged(it, adapter)
        }
    }

    private fun getStreamInfo(): StreamableKey {
        val args = requireArguments()
        return if (args.containsKey(ARG_EPISODE)) {
            args.getSerializable(ARG_EPISODE) as EpisodeKey
        } else {
            args.getSerializable(ARG_MOVIE_KEY) as StreamableKey
        }
    }

    private fun onStreamLoadingStateChanged(it: StreamsViewModel.State, adapter: StreamsAdapter) {
        if (it is StreamsViewModel.State.Success)
            adapter.items = it.info.streams
    }

    private fun onDirectLinkUnsupported() {
        Toast.makeText(requireContext(), R.string.stream_not_downloadable, Toast.LENGTH_SHORT)
            .show()
    }

    private fun onStreamClickedPlay(stream: FinalizedVideoInformation) {
        val direct = stream is FinalizedVideoInformation.Direct
        startVideo(stream.url, direct)
    }

    private fun onStreamClickedDownload(stream: FinalizedVideoInformation) {
        when (stream) {
            is FinalizedVideoInformation.Direct -> viewModel.downloadVideo(stream.url)
            is FinalizedVideoInformation.Website -> onDirectLinkUnsupported()
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
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            toast(R.string.video_player_not_installed)
        }
    }

    companion object {
        private const val ARG_MOVIE_KEY = "movie"
        private const val ARG_EPISODE = "episode"

        @JvmStatic
        fun newInstance(episode: EpisodeKey): StreamsFragment {
            val args = Bundle()
            args.putSerializable(ARG_EPISODE, episode)
            val fragment = StreamsFragment()
            fragment.arguments = args
            return fragment
        }

        @JvmStatic
        fun newInstance(movie: MovieKey): StreamsFragment {
            val args = Bundle()
            args.putSerializable(ARG_MOVIE_KEY, movie)
            val fragment = StreamsFragment()
            fragment.arguments = args
            return fragment
        }
    }
}