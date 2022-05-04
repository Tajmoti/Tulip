package com.tajmoti.tulip.ui.show

import android.content.Context
import com.tajmoti.libtulip.dto.SeasonEpisodeDto
import com.tajmoti.libtulip.ui.player.VideoPlayerUtils.episodeToLabel
import com.tajmoti.tulip.R
import com.tajmoti.tulip.databinding.ItemEpisodeBinding
import com.tajmoti.tulip.ui.base.BaseAdapter
import com.tajmoti.tulip.ui.utils.loadImageAsBackground

class EpisodesAdapter(
    val onPlayClickedListener: ((SeasonEpisodeDto) -> Unit),
    onDetailsClickedListener: ((SeasonEpisodeDto) -> Unit)
) : BaseAdapter<SeasonEpisodeDto, ItemEpisodeBinding>(
    ItemEpisodeBinding::inflate,
    onDetailsClickedListener
) {

    override fun onBindViewHolder(context: Context, index: Int, binding: ItemEpisodeBinding, item: SeasonEpisodeDto) {
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

    private fun getRating(context: Context, item: SeasonEpisodeDto): String {
        val ratingFraction = item.voteAverage ?: return context.getString(R.string.no_rating)
        return context.getString(R.string.rating_format, ratingFraction)
    }
}