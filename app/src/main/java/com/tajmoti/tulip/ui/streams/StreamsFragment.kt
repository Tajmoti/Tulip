package com.tajmoti.tulip.ui.streams

import android.app.DownloadManager
import android.content.Context.DOWNLOAD_SERVICE
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.tajmoti.tulip.BaseFragment
import com.tajmoti.tulip.R
import com.tajmoti.tulip.databinding.FragmentStreamsBinding
import com.tajmoti.tulip.model.StreamingService
import com.tajmoti.tulip.ui.setupWithAdapterAndDivider
import dagger.hilt.android.AndroidEntryPoint
import java.util.*


@AndroidEntryPoint
class StreamsFragment : BaseFragment<FragmentStreamsBinding, StreamsViewModel>(
    FragmentStreamsBinding::inflate
) {
    override val viewModel: StreamsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = StreamsAdapter(this::onStreamDownloadRequested)
        adapter.callback = this::onStreamClicked
        binding.viewModel = viewModel
        binding.recyclerSearch.setupWithAdapterAndDivider(adapter)
        viewModel.streamLoadingState.observe(viewLifecycleOwner) {
            onStreamLoadingStateChanged(it, adapter)
        }
        viewModel.directStreamLoadingState.observe(viewLifecycleOwner) { onDirectLoadingChanged(it) }
        val args = requireArguments()

        val streamInfo = if (args.containsKey(ARG_TV_SHOW_ID)) {
            val tvShow = args.getString(ARG_TV_SHOW_ID)!!
            val season = args.getString(ARG_SEASON_ID)!!
            val episode = args.getString(ARG_EPISODE_ID)!!
            StreamsViewModel.StreamInfo.TvShow(tvShow, season, episode)
        } else {
            val movie = args.getString(ARG_MOVIE_ID)!!
            StreamsViewModel.StreamInfo.Movie(movie)
        }
        val service = args.getSerializable(ARG_SERVICE) as StreamingService
        viewModel.fetchStreams(service, streamInfo)
    }

    private fun onStreamLoadingStateChanged(it: StreamsViewModel.State, adapter: StreamsAdapter) {
        if (it is StreamsViewModel.State.Success)
            adapter.items = it.streams
    }

    private fun onDirectLoadingChanged(it: StreamsViewModel.DirectStreamLoading?) {
        it ?: return
        if (it is StreamsViewModel.DirectStreamLoading.Success) {
            if (it.download) {
                downloadVideo(it.directLink)
            } else {
                startVideo(it.directLink, true)
            }
        } else if (it is StreamsViewModel.DirectStreamLoading.Failed) {
            startVideo(it.video.url, false)
        }
    }

    private fun onStreamClicked(stream: UnloadedVideoStreamRef) {
        if (stream.linkExtractionSupported) {
            viewModel.fetchStreamDirect(stream.info, false)
        } else {
            startVideo(stream.info.url, false)
        }
    }

    private fun onStreamDownloadRequested(stream: UnloadedVideoStreamRef) {
        if (stream.linkExtractionSupported) {
            viewModel.fetchStreamDirect(stream.info, true)
        } else {
            Toast.makeText(requireContext(), R.string.stream_not_downloadable, Toast.LENGTH_SHORT)
                .show()
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

    private fun downloadVideo(url: String) {
        downloadFileToFiles(Uri.parse(url))
    }

    private fun downloadFileToFiles(uri: Uri): Long {
        val downloadManager = requireContext().getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(uri)
        request.setTitle("Image Download")
            .setDescription("Image download using DownloadManager.")
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_MOVIES,
                UUID.randomUUID().toString()
            )
            .allowScanningByMediaScanner()
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        return downloadManager.enqueue(request)
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