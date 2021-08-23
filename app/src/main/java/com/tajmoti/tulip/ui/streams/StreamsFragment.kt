package com.tajmoti.tulip.ui.streams

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.tajmoti.libtulip.model.StreamingService
import com.tajmoti.libtulip.model.UnloadedVideoStreamRef
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.StreamableKey
import com.tajmoti.tulip.BaseFragment
import com.tajmoti.tulip.R
import com.tajmoti.tulip.databinding.FragmentStreamsBinding
import com.tajmoti.tulip.ui.setupWithAdapterAndDivider
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StreamsFragment : BaseFragment<FragmentStreamsBinding, StreamsViewModel>(
    FragmentStreamsBinding::inflate
) {
    override val viewModel: StreamsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val streamInfo = getStreamInfo()
        viewModel.fetchStreams(streamInfo)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = StreamsAdapter(this::onStreamClickedDownload)
        adapter.callback = this::onStreamClickedPlay
        binding.viewModel = viewModel
        binding.recyclerSearch.setupWithAdapterAndDivider(adapter)
        viewModel.streamLoadingState.observe(viewLifecycleOwner) {
            onStreamLoadingStateChanged(it, adapter)
        }
        viewModel.linkLoadingState.observe(viewLifecycleOwner) {
            onDirectLoadingChanged(it)
        }
    }

    private fun getStreamInfo(): StreamableKey {
        val args = requireArguments()
        val service = args.getSerializable(ARG_SERVICE) as StreamingService
        return if (args.containsKey(ARG_TV_SHOW_ID)) {
            val tvShow = args.getString(ARG_TV_SHOW_ID)!!
            val season = args.getString(ARG_SEASON_ID)!!
            val episode = args.getString(ARG_EPISODE_ID)!!
            EpisodeKey(service, tvShow, season, episode)
        } else {
            val movie = args.getString(ARG_MOVIE_ID)!!
            MovieKey(service, movie)
        }
    }

    private fun onStreamLoadingStateChanged(it: StreamsViewModel.State, adapter: StreamsAdapter) {
        if (it is StreamsViewModel.State.Success)
            adapter.items = it.streams
    }

    private fun onDirectLoadingChanged(it: StreamsViewModel.LinkLoadingState?) {
        it ?: return
        if (it is StreamsViewModel.LinkLoadingState.LoadedDirect) {
            onDirectLinkLoaded(it)
        } else if (it is StreamsViewModel.LinkLoadingState.DirectLinkUnsupported) {
            onDirectLinkUnsupported(it)
        }
    }

    private fun onDirectLinkLoaded(it: StreamsViewModel.LinkLoadingState.LoadedDirect) {
        if (!it.download) {
            startVideo(it.directLink, true)
        }
    }

    private fun onDirectLinkUnsupported(it: StreamsViewModel.LinkLoadingState.DirectLinkUnsupported) {
        if (!it.download) {
            startVideo(it.stream.url, false)
        } else {
            Toast.makeText(requireContext(), R.string.stream_not_downloadable, Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun onStreamClickedPlay(stream: UnloadedVideoStreamRef) {
        viewModel.onStreamClicked(stream, false)
    }

    private fun onStreamClickedDownload(stream: UnloadedVideoStreamRef) {
        viewModel.onStreamClicked(stream, true)
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
        private const val ARG_SERVICE = "service"
        private const val ARG_MOVIE_ID = "movie"
        private const val ARG_TV_SHOW_ID = "tv_show"
        private const val ARG_SEASON_ID = "season"
        private const val ARG_EPISODE_ID = "episode"

        @JvmStatic
        fun newInstance(
            service: StreamingService,
            tvShow: String,
            season: String,
            key: String
        ): StreamsFragment {
            val args = Bundle()
            args.putSerializable(ARG_SERVICE, service)
            args.putString(ARG_TV_SHOW_ID, tvShow)
            args.putString(ARG_SEASON_ID, season)
            args.putString(ARG_EPISODE_ID, key)
            val fragment = StreamsFragment()
            fragment.arguments = args
            return fragment
        }

        @JvmStatic
        fun newInstance(service: StreamingService, key: String): StreamsFragment {
            val args = Bundle()
            args.putSerializable(ARG_SERVICE, service)
            args.putString(ARG_MOVIE_ID, key)
            val fragment = StreamsFragment()
            fragment.arguments = args
            return fragment
        }
    }
}