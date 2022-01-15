package com.tajmoti.tulip.ui.show

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.tajmoti.libtulip.model.info.TulipEpisodeInfo
import com.tajmoti.libtulip.ui.player.VideoPlayerUtils.episodeToLabel
import com.tajmoti.tulip.R
import com.tajmoti.tulip.databinding.ItemEpisodeBinding
import com.tajmoti.tulip.ui.base.BaseAdapter
import com.tajmoti.tulip.ui.utils.loadImageAsBackground

class EpisodesAdapter(
    val onPlayClickedListener: ((TulipEpisodeInfo) -> Unit),
    onDetailsClickedListener: ((TulipEpisodeInfo) -> Unit),
    headerCreator: ((LayoutInflater, ViewGroup, Boolean) -> ViewBinding)? = null
) : BaseAdapter<TulipEpisodeInfo, ItemEpisodeBinding>(
    ItemEpisodeBinding::inflate,
    onDetailsClickedListener,
    headerCreator
) {

    override fun onBindViewHolder(context: Context, index: Int, binding: ItemEpisodeBinding, item: TulipEpisodeInfo) {
        binding.episodeNumberAndTitle = episodeToLabel(item)
        binding.episodeRatingStr = getRating(context, item)
        binding.episodeDescription = item.overview
        binding.imageEpisode.setOnClickListener { onPlayClickedListener(item) }
        item.stillPath?.let { stillPath ->
            binding.imageEpisode.loadImageAsBackground(
                stillPath,
                R.drawable.ic_baseline_live_tv_128
            )
        }
    }

    private fun getRating(context: Context, item: TulipEpisodeInfo): String {
        val tmdbItem =
            item as? TulipEpisodeInfo.Tmdb ?: return context.getString(R.string.no_rating)
        val ratingFraction = tmdbItem.voteAverage ?: return context.getString(R.string.no_rating)
        return context.getString(R.string.rating_format, ratingFraction)
    }
}