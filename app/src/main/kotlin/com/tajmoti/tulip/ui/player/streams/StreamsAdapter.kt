package com.tajmoti.tulip.ui.player.streams

import android.content.Context
import com.tajmoti.libtulip.dto.StreamingSiteLinkDto
import com.tajmoti.tulip.R
import com.tajmoti.tulip.databinding.ItemStreamBinding
import com.tajmoti.tulip.ui.base.BaseIdentityAdapter
import com.tajmoti.tulip.ui.languageToIcon

class StreamsAdapter(
    onPlayClickedListener: (StreamingSiteLinkDto) -> Unit,
    val onDownloadClickListener: (StreamingSiteLinkDto) -> Unit
) : BaseIdentityAdapter<StreamingSiteLinkDto, ItemStreamBinding>(
    ItemStreamBinding::inflate,
    onPlayClickedListener
) {

    override fun onBindViewHolder(
        context: Context,
        index: Int,
        binding: ItemStreamBinding,
        item: StreamingSiteLinkDto
    ) {
        val string = "#${index + 1}: ${item.serviceName}"
        binding.root.text = string
        binding.root.setCompoundDrawablesRelativeWithIntrinsicBounds(
            getDrawable(item),
            0,
            languageToIcon(item.language) ?: 0,
            0
        )
        binding.root.setOnLongClickListener { onDownloadClickListener(item); true }
    }

    private fun getDrawable(ref: StreamingSiteLinkDto): Int {
        return if (ref.linkExtractionSupported) {
            R.drawable.ic_baseline_ondemand_video_24
        } else {
            R.drawable.ic_baseline_web_24
        }
    }
}