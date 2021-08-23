package com.tajmoti.tulip.ui.streams

import com.tajmoti.libtulip.model.UnloadedVideoStreamRef
import com.tajmoti.tulip.BaseAdapter
import com.tajmoti.tulip.R
import com.tajmoti.tulip.databinding.ItemStreamBinding

class StreamsAdapter(
    val downloadCallback: (UnloadedVideoStreamRef) -> Unit
) : BaseAdapter<UnloadedVideoStreamRef, ItemStreamBinding>(ItemStreamBinding::inflate) {

    override fun onBindViewHolder(vh: Holder<ItemStreamBinding>, item: UnloadedVideoStreamRef) {
        val string = "#${vh.adapterPosition + 1}: ${item.info.serviceName}"
        vh.binding.root.text = string
        vh.binding.root.setCompoundDrawablesRelativeWithIntrinsicBounds(getDrawable(item), 0, 0, 0)
        vh.binding.root.setOnLongClickListener { downloadCallback(item); true }
    }

    private fun getDrawable(ref: UnloadedVideoStreamRef): Int {
        return if (ref.linkExtractionSupported) {
            R.drawable.ic_baseline_ondemand_video_24
        } else {
            R.drawable.ic_baseline_web_24
        }
    }
}