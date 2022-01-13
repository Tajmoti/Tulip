package com.tajmoti.tulip.ui.player.streams

import android.content.Context
import com.tajmoti.libtulip.model.stream.UnloadedVideoStreamRef
import com.tajmoti.tulip.R
import com.tajmoti.tulip.databinding.ItemStreamBinding
import com.tajmoti.tulip.ui.base.BaseIdentityAdapter
import com.tajmoti.tulip.ui.languageToIcon

class StreamsAdapter(
    onPlayClickedListener: (UnloadedVideoStreamRef) -> Unit,
    val onDownloadClickListener: (UnloadedVideoStreamRef) -> Unit
) : BaseIdentityAdapter<UnloadedVideoStreamRef, ItemStreamBinding>(
    ItemStreamBinding::inflate,
    onPlayClickedListener
) {

    override fun onBindViewHolder(
        context: Context,
        index: Int,
        binding: ItemStreamBinding,
        item: UnloadedVideoStreamRef
    ) {
        val string = "#${index + 1}: ${item.info.serviceName}"
        binding.root.text = string
        binding.root.setCompoundDrawablesRelativeWithIntrinsicBounds(
            getDrawable(item),
            0,
            languageToIcon(item.language) ?: 0,
            0
        )
        binding.root.setOnLongClickListener { onDownloadClickListener(item); true }
    }

    private fun getDrawable(ref: UnloadedVideoStreamRef): Int {
        return if (ref.linkExtractionSupported) {
            R.drawable.ic_baseline_ondemand_video_24
        } else {
            R.drawable.ic_baseline_web_24
        }
    }
}