package com.tajmoti.tulip.ui.streams

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.tajmoti.tulip.BaseFragment
import com.tajmoti.tulip.databinding.FragmentStreamsBinding
import com.tajmoti.tulip.model.StreamingService
import com.tajmoti.tulip.ui.setupWithAdapterAndDivider
import dagger.hilt.android.AndroidEntryPoint

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
        val args = requireArguments()

        val streamInfo = if (args.containsKey(ARG_TV_SHOW_ID)) {
            val tvShow = requireArguments().getString(ARG_TV_SHOW_ID)!!
            val season = requireArguments().getString(ARG_SEASON_ID)!!
            val episode = requireArguments().getString(ARG_EPISODE_ID)!!
            StreamsViewModel.StreamInfo.TvShow(tvShow, season, episode)
        } else {
            val movie = args.getString(ARG_MOVIE_ID)!!
            StreamsViewModel.StreamInfo.Movie(movie)
        }
        viewModel.fetchStreams(StreamingService.PRIMEWIRE, streamInfo)
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
        private const val ARG_MOVIE_ID = "movie"
        private const val ARG_TV_SHOW_ID = "tv_show"
        private const val ARG_SEASON_ID = "season"
        private const val ARG_EPISODE_ID = "episode"

        @JvmStatic
        fun newInstance(tvShow: String, season: String, key: String): StreamsFragment {
            val args = Bundle()
            args.putString(ARG_TV_SHOW_ID, tvShow)
            args.putString(ARG_SEASON_ID, season)
            args.putString(ARG_EPISODE_ID, key)
            val fragment = StreamsFragment()
            fragment.arguments = args
            return fragment
        }

        @JvmStatic
        fun newInstance(key: String): StreamsFragment {
            val args = Bundle()
            args.putString(ARG_MOVIE_ID, key)
            val fragment = StreamsFragment()
            fragment.arguments = args
            return fragment
        }
    }
}