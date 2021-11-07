package com.tajmoti.tulip.ui.player.streams

import com.tajmoti.libtulip.model.stream.UnloadedVideoStreamRef
import com.tajmoti.tulip.R
import com.tajmoti.tulip.databinding.ItemStreamBinding
import com.tajmoti.tulip.ui.BaseAdapter
import com.tajmoti.tulip.ui.languageToIcon

class StreamsAdapter(
    onPlayClickedListener: (UnloadedVideoStreamRef) -> Unit,
    val onDownloadClickListener: (UnloadedVideoStreamRef) -> Unit
) : BaseAdapter<UnloadedVideoStreamRef, ItemStreamBinding>(
    ItemStreamBinding::inflate,
    onPlayClickedListener
) {

    override fun onBindViewHolder(
        vh: Holder<ItemStreamBinding>,
        item: UnloadedVideoStreamRef
    ) {
        val string = "#${vh.adapterPosition + 1}: ${item.info.serviceName}"
        vh.binding.root.text = string
        vh.binding.root.setCompoundDrawablesRelativeWithIntrinsicBounds(
            getDrawable(item),
            0,
            languageToIcon(item.language) ?: 0,
            0
        )
        vh.binding.root.setOnLongClickListener { onDownloadClickListener(item); true }
    }

    private fun getDrawable(ref: UnloadedVideoStreamRef): Int {
        return if (ref.linkExtractionSupported) {
            R.drawable.ic_baseline_ondemand_video_24
        } else {
            R.drawable.ic_baseline_web_24
        }
    }
}