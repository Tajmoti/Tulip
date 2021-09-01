package com.tajmoti.tulip.ui.streams

import com.tajmoti.libtulip.model.stream.FinalizedVideoInformation
import com.tajmoti.tulip.R
import com.tajmoti.tulip.databinding.ItemStreamBinding
import com.tajmoti.tulip.ui.BaseAdapter
import com.tajmoti.tulip.ui.languageToIcon

class StreamsAdapter(
    val downloadCallback: (FinalizedVideoInformation) -> Unit
) : BaseAdapter<FinalizedVideoInformation, ItemStreamBinding>(ItemStreamBinding::inflate) {

    override fun onBindViewHolder(
        vh: Holder<ItemStreamBinding>,
        item: FinalizedVideoInformation
    ) {
        val string = "#${vh.adapterPosition + 1}: ${item.serviceName} ${(item as? FinalizedVideoInformation.Direct)?.dimensions}"
        vh.binding.root.text = string
        vh.binding.root.setCompoundDrawablesRelativeWithIntrinsicBounds(
            getDrawable(item),
            0,
            languageToIcon(item.language) ?: 0,
            0
        )
        vh.binding.root.setOnLongClickListener { downloadCallback(item); true }
    }

    private fun getDrawable(ref: FinalizedVideoInformation): Int {
        return if (ref is FinalizedVideoInformation.Direct) {
            R.drawable.ic_baseline_ondemand_video_24
        } else {
            R.drawable.ic_baseline_web_24
        }
    }
}